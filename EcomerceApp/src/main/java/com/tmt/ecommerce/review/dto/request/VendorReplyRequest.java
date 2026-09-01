package com.tmt.ecommerce.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VendorReplyRequest(
    @NotBlank(message = "Nội dung phản hồi không được để trống") @Size(max = 1000, message = "Nội dung phản hồi tối đa 1000 ký tự") String replyComment
) {}
