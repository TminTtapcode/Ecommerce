package com.tmt.ecommerce.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopCreateRequest {

    @NotBlank(message = "Tên shop không được để trống")
    @Size(min = 3, max = 100, message = "Tên shop phải từ 3 đến 100 ký tự")
    private String name;

    private String description;
}
