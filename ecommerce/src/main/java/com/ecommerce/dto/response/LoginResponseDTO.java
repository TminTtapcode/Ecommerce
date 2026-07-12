package com.ecommerce.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    private String token;
    private String email;
    private List<String> roles;
}