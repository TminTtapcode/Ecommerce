package com.tmt.ecommerce.order.repository;

import com.tmt.ecommerce.order.entity.OrderAdminStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;

public interface OrderAdminStatusHistoryRepository extends JpaRepository<OrderAdminStatusHistory, Long> {
    Page<OrderAdminStatusHistory> findByOrderIdOrderByIdDesc(Long orderId, Pageable pageable);
}
