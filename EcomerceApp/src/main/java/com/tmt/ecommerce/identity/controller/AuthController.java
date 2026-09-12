package com.tmt.ecommerce.identity.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.identity.dto.AuthResponse;
import com.tmt.ecommerce.identity.dto.UserLoginRequest;
import com.tmt.ecommerce.identity.dto.UserRegisterRequest;
import com.tmt.ecommerce.identity.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Đăng ký tài khoản thành công")
                        .data(authResponse)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(
            @Valid @RequestBody UserLoginRequest request) {

        AuthResponse authResponse = authService.authenticate(request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(authResponse)
                .build());
    }
}
