package com.tmt.ecommerce.shop.dto;

public record ShopBanRequest(@jakarta.validation.constraints.Size(max = 500) String reason) {}
