package com.tmt.ecommerce.shop.api;

public interface ShopInternalService {
    java.util.List<Long> getBannedShopIds();
    /** Requires caller transaction; shared lock coordinates new business with ban/unban. */
    void requireNotBannedForSale(Long shopId);

    /**
     * Kiểm tra xem userId có phải là chủ sở hữu của shopId hay không.
     */
    boolean isShopOwner(Long shopId, Long userId);

    /**
     * Lấy shopId của Vendor theo userId. Nếu user chưa có Shop, ném exception.
     */
    Long getShopIdByUserId(Long userId);

    /**
     * Lấy shopId của Vendor theo userId. Trả về Optional rỗng nếu không tìm thấy.
     */
    java.util.Optional<Long> findShopIdByUserId(Long userId);

    /**
     * Lấy userId của chủ sở hữu Shop theo shopId. Trả về Optional rỗng nếu không tìm thấy.
     */
    java.util.Optional<Long> getUserIdByShopId(Long shopId);

    /**
     * Lấy tên Shop theo shopId. Trả về Optional rỗng nếu không tìm thấy.
     */
    java.util.Optional<String> getShopNameById(Long shopId);

    /** Count total shops — for Admin Dashboard. */
    long countShops();
}
