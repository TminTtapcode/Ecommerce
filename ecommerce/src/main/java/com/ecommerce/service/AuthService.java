package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequestDTO;
import com.ecommerce.dto.response.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}