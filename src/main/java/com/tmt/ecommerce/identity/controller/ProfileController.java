package com.tmt.ecommerce.identity.controller;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.identity.dto.*;
import com.tmt.ecommerce.identity.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final ProfileService profiles;
    @GetMapping
    public ApiResponse<ProfileResponse> get() {
        return ApiResponse.<ProfileResponse>builder().status(200).data(profiles.get()).build();
    }
    @PutMapping
    public ApiResponse<ProfileResponse> update(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.<ProfileResponse>builder().status(200).message("Đã cập nhật hồ sơ")
                .data(profiles.update(request)).build();
    }
}
