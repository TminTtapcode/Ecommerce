package com.tmt.ecommerce.identity.api;

public interface IdentityInternalService {
    String assignShopOwnerRole(Long userId);
    String getUserFullNameOrDefault(Long userId);
}
