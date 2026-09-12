package com.tmt.ecommerce.notification;
import com.tmt.ecommerce.notification.listener.NotificationPushListener;
import com.tmt.ecommerce.notification.service.NotificationWriter;
import com.tmt.ecommerce.notification.dto.response.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class NotificationPushListenerTest {
    @Test void deliveryFailureDoesNotEscapeAfterCommitHandler() {
        var transport=mock(SimpMessagingTemplate.class);
        var dto=new NotificationResponse(1L,"ORDER_SHIPPED","Title","Body","2",false,java.time.LocalDateTime.now());
        doThrow(new IllegalStateException("broker unavailable")).when(transport).convertAndSendToUser("3","/queue/notifications",dto);
        assertDoesNotThrow(() -> new NotificationPushListener(transport).created(new NotificationWriter.Created(3L,dto)));
        verify(transport).convertAndSendToUser("3","/queue/notifications",dto);
    }
}
