package com.tmt.ecommerce.shop.api;

public interface ShopInternalService {
    /**
     * Kiểm tra xem userId có phải là chủ sở hữu của shopId hay không.
     */
    boolean isShopOwner(Long shopId, Long userId);

    /**
     * Lấy shopId của Vendor theo userId. Nếu user chưa có Shop, ném exception.
     */
    Long getShopIdByUserId(Long userId);
}
