package com.tmt.ecommerce.identity.service;

import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.identity.entity.Role;
import com.tmt.ecommerce.identity.entity.User;
import com.tmt.ecommerce.identity.repository.RoleRepository;
import com.tmt.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityInternalServiceImpl implements IdentityInternalService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public String assignShopOwnerRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ sở hữu."));

        Role shopRole = roleRepository.findByName("ROLE_SHOP_OWNER")
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình quyền ROLE_SHOP_OWNER."));

        if (!user.getRoles().contains(shopRole)) {
            user.getRoles().add(shopRole);
            userRepository.save(user);
        }
        
        return user.getEmail();
    }

    @Override
    public String getUserFullNameOrDefault(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                        return user.getFullName();
                    }
                    String email = user.getEmail();
                    int atIndex = email.indexOf('@');
                    if (atIndex > 3) {
                        return email.substring(0, 3) + "***" + email.substring(atIndex);
                    }
                    return email;
                })
                .orElse("Khách hàng");
    }
}
