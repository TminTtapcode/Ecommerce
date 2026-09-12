package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.shop.api.event.ShopStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ShopStatusAuditListener {
    @TransactionalEventListener
    public void audit(ShopStatusChangedEvent event) {
        log.info("event=shop_status_changed actorId={} shopId={} oldStatus={} newStatus={} reason={}",
                event.actorId(), event.shopId(), event.oldStatus(), event.newStatus(),
                event.reason() == null ? null : event.reason().replaceAll("[\\r\\n\\t]", " "));
    }
}
