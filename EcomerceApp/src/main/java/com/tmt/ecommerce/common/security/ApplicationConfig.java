package com.tmt.ecommerce.common.security;

import com.tmt.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // 1. Chỉ cho Spring cách tìm User dưới Database bằng Email
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + username));
    }

    // 2. Bean này dùng để Băm (Hash) mật khẩu bằng thuật toán Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Cập nhật cho Spring Security 7.x: Khởi tạo thông qua Constructor thay vì dùng Setter
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

    // 4. Manager quản lý toàn bộ luồng Authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}