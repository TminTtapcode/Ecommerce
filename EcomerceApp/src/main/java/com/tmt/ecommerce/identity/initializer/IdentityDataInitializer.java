package com.tmt.ecommerce.identity.initializer;

import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class IdentityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already exist. Skipping IdentityDataInitializer.");
            return;
        }

        log.info("=== STARTING IDENTITY SEED DATA ===");

        Role roleUser = getOrCreateRole("ROLE_USER");
        Role roleVendor = getOrCreateRole("ROLE_VENDOR");
        Role roleAdmin = getOrCreateRole("ROLE_ADMIN");

        String encodedPassword = passwordEncoder.encode("password123");

        createUser("admin@ecommerce.com", "Trần Quản Trị", "0901000001", encodedPassword, Set.of(roleAdmin, roleUser));

        createUser("vendor1@techstore.com", "Nguyễn Văn Tech", "0902000001", encodedPassword, Set.of(roleVendor, roleUser));
        createUser("vendor2@fashionhub.com", "Lê Thị Thời Trang", "0902000002", encodedPassword, Set.of(roleVendor, roleUser));
        createUser("vendor3@giadungxanh.com", "Phạm Văn Gia Dụng", "0902000003", encodedPassword, Set.of(roleVendor, roleUser));

        createUser("customer1@gmail.com", "Đặng Minh Triết", "0903000001", encodedPassword, Set.of(roleUser));
        createUser("customer2@gmail.com", "Vũ Hoàng Nam", "0903000002", encodedPassword, Set.of(roleUser));
        createUser("customer3@gmail.com", "Trần Thị Mai", "0903000003", encodedPassword, Set.of(roleUser));
        createUser("customer4@gmail.com", "Ngô Quốc Bảo", "0903000004", encodedPassword, Set.of(roleUser));
        createUser("customer5@gmail.com", "Hoàng Bích Ngọc", "0903000005", encodedPassword, Set.of(roleUser));
        createUser("customer6@gmail.com", "Lý Thanh Tùng", "0903000006", encodedPassword, Set.of(roleUser));

        log.info("=== FINISHED IDENTITY SEED DATA ===");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

    private User createUser(String email, String fullName, String phone, String encodedPassword, Set<Role> roles) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .phone(phone)
                .passwordHash(encodedPassword)
                .status("ACTIVE")
                .roles(roles)
                .build();
        return userRepository.save(user);
    }
}
