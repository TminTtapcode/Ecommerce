package com.tmt.ecommerce.order.entity;

public enum OrderStatus {
    PENDING,    // Chờ xác nhận
    CONFIRMED,  // Đã xác nhận, đang chuẩn bị hàng
    SHIPPED,    // Đang giao hàng
    DELIVERED,  // Đã giao thành công
    CANCELLED   // Đã hủy
}