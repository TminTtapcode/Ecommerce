package com.tmt.ecommerce.order.initializer;

import com.tmt.ecommerce.common.constant.SeedDataIds;
import com.tmt.ecommerce.order.entity.Order;
import com.tmt.ecommerce.order.entity.OrderItem;
import com.tmt.ecommerce.order.entity.OrderStatus;
import com.tmt.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@org.springframework.core.annotation.Order(5)
@RequiredArgsConstructor
public class OrderDataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            log.info("Orders already exist. Skipping OrderDataInitializer.");
            return;
        }

        log.info("=== STARTING ORDER SEED DATA ===");

        Order order1 = createOrder(SeedDataIds.CUSTOMER_1_ID, SeedDataIds.SHOP_1_ID, SeedDataIds.PAYMENT_GROUP_1, OrderStatus.DELIVERED, "123 Đường Nguyễn Huệ, Q1, TP.HCM", "VNPAY", new BigDecimal("29990000"));
        addOrderItem(order1, SeedDataIds.VARIANT_P1_1_ID, "iPhone 15 Pro Max 256GB", 1, new BigDecimal("29990000"));

        Order order2 = createOrder(SeedDataIds.CUSTOMER_2_ID, SeedDataIds.SHOP_2_ID, SeedDataIds.PAYMENT_GROUP_2, OrderStatus.CONFIRMED, "456 Đường Cầu Giấy, Hà Nội", "COD", new BigDecimal("890000"));
        addOrderItem(order2, SeedDataIds.VARIANT_P3_1_ID, "Áo Polo Nam Nike Dri-FIT", 1, new BigDecimal("890000"));

        Order order3 = createOrder(SeedDataIds.CUSTOMER_3_ID, SeedDataIds.SHOP_3_ID, SeedDataIds.PAYMENT_GROUP_3, OrderStatus.SHIPPED, "789 Đường Trần Hưng Đạo, Đà Nẵng", "VNPAY", new BigDecimal("1850000"));
        addOrderItem(order3, SeedDataIds.VARIANT_P5_1_ID, "Nồi chiên không dầu Sony AirFryer 5.5L", 1, new BigDecimal("1850000"));

        log.info("=== FINISHED ORDER SEED DATA ===");
    }

    private Order createOrder(Long userId, Long shopId, String paymentGroupId, OrderStatus status, String address, String paymentMethod, BigDecimal total) {
        Order order = Order.builder()
                .userId(userId)
                .shopId(shopId)
                .paymentGroupId(paymentGroupId)
                .status(status)
                .shippingAddress(address)
                .paymentMethod(paymentMethod)
                .totalAmount(total)
                .build();
        return orderRepository.save(order);
    }

    private void addOrderItem(Order order, Long variantId, String name, Integer qty, BigDecimal price) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .productVariantId(variantId)
                .productName(name)
                .quantity(qty)
                .unitPrice(price)
                .subTotal(price.multiply(BigDecimal.valueOf(qty)))
                .build();
        order.addOrderItem(item);
        orderRepository.save(order);
    }
}
