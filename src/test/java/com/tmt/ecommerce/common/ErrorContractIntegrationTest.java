package com.tmt.ecommerce.common;

import com.tmt.ecommerce.common.security.JwtService;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.UserRepository;
import com.tmt.ecommerce.identity.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorContractIntegrationTest {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;
    @org.springframework.beans.factory.annotation.Value("${application.security.jwt.secret-key}") String signingKey;
    private String token;
    private String email;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach void setup() {
        email = "s102-" + UUID.randomUUID() + "@example.test";
        User user = users.save(User.builder().email(email).fullName("Error contract")
                .passwordHash("unused-test-password-hash").status("ACTIVE").build());
        token = jwt.generateToken(new CustomUserDetails(user));
    }
    private HttpResponse<String> request(String path, String bearer, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
        if (body != null) builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private JsonNode error(HttpResponse<String> response, int status, String code) throws Exception {
        assertEquals(status, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
        JsonNode body = mapper.readTree(response.body());
        assertEquals(status, body.get("status").asInt());
        assertFalse(body.get("success").asBoolean());
        assertEquals(code, body.get("errorCode").asText());
        assertTrue(body.has("data") && body.get("data").isNull());
        return body;
    }
    @Test void anonymousRequestUsesSecurityEntryPoint() throws Exception {
        error(request("/api/v1/users/me", null, null), 401, "UNAUTHORIZED");
    }
    @Test void malformedJwtUsesSecurityEntryPoint() throws Exception {
        error(request("/api/v1/users/me", "malformed-token", null), 401, "UNAUTHORIZED");
    }
    @Test void expiredJwtUsesSecurityEntryPoint() throws Exception {
        String expired = io.jsonwebtoken.Jwts.builder().subject(email)
                .expiration(new java.util.Date(System.currentTimeMillis() - 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(signingKey)))
                .compact();
        error(request("/api/v1/users/me", expired, null), 401, "UNAUTHORIZED");
    }
    @Test void nonAdminCannotListShops() throws Exception {
        error(request("/api/v1/admin/shops", token, null), 403, "ACCESS_DENIED");
    }
    @Test void currentUserWithoutShopHasDomainCode() throws Exception {
        error(request("/api/v1/shops/my-shop", token, null), 404, "SHOP_NOT_FOUND");
    }
    @Test void registrationValidationKeepsFieldDetails() throws Exception {
        JsonNode body = error(request("/api/v1/auth/register", null, "{}"), 400, "INVALID_INPUT");
        assertTrue(body.has("fieldErrors"));
    }
    @Test void duplicateEmailHasSymbolicCode() throws Exception {
        error(request("/api/v1/auth/register", null,
                "{\"email\":\"" + email + "\",\"password\":\"password123\",\"fullName\":\"Test user\"}"), 400, "EMAIL_ALREADY_EXISTS");
    }
    @Test void publicInvalidPageIsBadRequest() throws Exception {
        error(request("/api/v1/products?page=not-a-number", null, null), 400, "INVALID_INPUT");
    }
    @Test void missingPublicRouteIsNotFound() throws Exception {
        error(request("/api/v1/auth/no-such-route", null, null), 404, "NOT_FOUND");
    }
    @Test void ipnKeepsGatewayEnvelope() throws Exception {
        var response = request("/api/v1/payments/vnpay/ipn?vnp_TxnRef=invalid", null, null);
        assertEquals(200, response.statusCode());
        var body = mapper.readTree(response.body());
        assertEquals("99", body.get("RspCode").asText());
        assertFalse(body.has("success"));
    }
}
