package com.tmt.ecommerce.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmt.ecommerce.common.security.JwtService;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShopApprovalIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private JwtService jwtService;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    private User createTestUser(String email, String roleName) {
        User user = User.builder()
                .email(email)
                .passwordHash("encoded_password")
                .fullName("Test " + roleName)
                .status("ACTIVE")
                .build();
        
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role r = new Role();
            r.setName(roleName);
            return roleRepository.save(r);
        });
        user.setRoles(java.util.Set.of(role));
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Shop Approval Flow: User requests shop -> Admin approves -> User becomes VENDOR")
    void testShopApprovalFlow() throws Exception {
        // 1. Setup User and Admin
        String uniqueSuffix = System.currentTimeMillis() + "";
        User user = createTestUser("user_" + uniqueSuffix + "@example.com", "ROLE_USER");
        User admin = createTestUser("admin_" + uniqueSuffix + "@example.com", "ROLE_ADMIN");

        // Ensure ROLE_VENDOR exists for assignment
        if (roleRepository.findByName("ROLE_VENDOR").isEmpty()) {
            Role r = new Role();
            r.setName("ROLE_VENDOR");
            roleRepository.save(r);
        }

        String userToken = jwtService.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(user));
        String adminToken = jwtService.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(admin));

        // 2. User tries to access Vendor API -> Should be 403 Forbidden
        HttpRequest getVendorOrdersReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/vendor/orders"))
                .header("Authorization", "Bearer " + userToken)
                .GET()
                .build();
        HttpResponse<String> vendorResp1 = httpClient.send(getVendorOrdersReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, vendorResp1.statusCode(), "User without VENDOR role should get 403");

        // 3. User creates a Shop
        ShopCreateRequest createShopReq = new ShopCreateRequest();
        createShopReq.setName("My Test Shop " + uniqueSuffix);
        createShopReq.setDescription("A shop for testing");

        HttpRequest createShopHttpReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/shops"))
                .header("Authorization", "Bearer " + userToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createShopReq)))
                .build();
        HttpResponse<String> createShopResp = httpClient.send(createShopHttpReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createShopResp.statusCode(), "Shop should be created successfully");

        // Verify Shop is PENDING in DB
        List<Shop> shops = shopRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .toList();
        assertEquals(1, shops.size());
        Shop createdShop = shops.get(0);
        assertEquals(ShopStatus.PENDING, createdShop.getStatus());
        assertEquals(user.getId(), createdShop.getUserId());

        // 4. Admin approves the Shop
        HttpRequest approveShopReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/admin/shops/" + createdShop.getId() + "/approve"))
                .header("Authorization", "Bearer " + adminToken)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> approveShopResp = httpClient.send(approveShopReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, approveShopResp.statusCode(), "Admin should approve shop successfully");

        // 5. Verify DB state: Shop is ACTIVE, User has ROLE_VENDOR
        Shop updatedShop = shopRepository.findById(createdShop.getId()).orElseThrow();
        assertEquals(ShopStatus.ACTIVE, updatedShop.getStatus());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        boolean hasVendorRole = updatedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_VENDOR"));
        assertTrue(hasVendorRole, "User should have ROLE_VENDOR after shop approval");

        boolean hasShopOwnerRole = updatedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SHOP_OWNER"));
        assertFalse(hasShopOwnerRole, "System should not use ROLE_SHOP_OWNER anymore");

        // 6. User accesses Vendor API again with NEW TOKEN (roles updated)
        String newUserToken = jwtService.generateToken(new com.tmt.ecommerce.identity.security.CustomUserDetails(updatedUser));
        HttpRequest getVendorOrdersReq2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/vendor/orders"))
                .header("Authorization", "Bearer " + newUserToken)
                .GET()
                .build();
        HttpResponse<String> vendorResp2 = httpClient.send(getVendorOrdersReq2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, vendorResp2.statusCode(), "User with VENDOR role should get 200 OK");
    }
}
