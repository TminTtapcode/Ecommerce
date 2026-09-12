package com.tmt.ecommerce.chat.repository;

import com.tmt.ecommerce.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByBuyerIdAndShopId(Long buyerId, Long shopId);
    List<ChatRoom> findAllByBuyerIdOrderByUpdatedAtDesc(Long buyerId);
    List<ChatRoom> findAllByShopIdOrderByUpdatedAtDesc(Long shopId);
}
