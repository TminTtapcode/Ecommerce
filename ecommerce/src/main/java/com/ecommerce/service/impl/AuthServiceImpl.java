package com.ecommerce.service.impl;

import com.ecommerce.dto.request.LoginRequestDTO;
import com.ecommerce.dto.response.LoginResponseDTO;
import com.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Dependency Injection thông qua Constructor (nhờ Lombok)
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService; // Tưởng tượng em đã có service sinh token này

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Nhờ Spring Security kiểm tra email và password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Nếu code chạy đến đây nghĩa là pass, lấy thông tin User ra
        org.springframework.security.core.userdetails.User userDetails = 
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // 3. Lấy danh sách Role
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 4. Sinh JWT Token
        String token = jwtService.generateToken(userDetails);

        // 5. Trả về DTO
        return LoginResponseDTO.builder()
                .token(token)
                .email(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}