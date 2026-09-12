package com.tmt.ecommerce.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    // 1. Bean này dùng để Băm (Hash) mật khẩu bằng thuật toán Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cập nhật cho Spring Security 7.x: Khởi tạo thông qua Constructor thay vì dùng Setter
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // Truyền thẳng userDetailsService vào constructor (Bắt buộc trong Security 7+)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // PasswordEncoder có thể vẫn giữ setter, hoặc nếu Security 7.0.8 yêu cầu cả 2,
        // em có thể đổi thành: new DaoAuthenticationProvider(userDetailsService, passwordEncoder);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    // 3. Manager quản lý toàn bộ luồng Authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}