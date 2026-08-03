package com.tmt.ecommerce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async // Đẩy hàm này ra một luồng riêng biệt, không block luồng chính
    public void sendShopApprovalNotification(String toEmail, String shopName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🎉 Chúc mừng! Gian hàng của bạn đã được phê duyệt");
            message.setText("Chào bạn,\n\n" +
                    "Gian hàng '" + shopName + "' của bạn đã được ban quản trị phê duyệt thành công.\n" +
                    "Bây giờ bạn đã có thể đăng nhập lại vào hệ thống để bắt đầu đăng tải sản phẩm và kinh doanh.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ TMT E-commerce");

            mailSender.send(message);
            log.info("Đã gửi email thông báo phê duyệt thành công tới: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            // Vì chạy @Async, lỗi ở đây sẽ không làm hỏng transaction duyệt shop của Admin
        }
    }
}