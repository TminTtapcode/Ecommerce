package com.tmt.ecommerce.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewUpdateRequest(
    @NotNull(message = "Đánh giá sao không được để trống") @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao") @Max(value = 5, message = "Đánh giá tối đa là 5 sao") Integer rating,
    @NotBlank(message = "Nội dung nhận xét không được để trống") @Size(max = 1000, message = "Nội dung nhận xét tối đa 1000 ký tự") String comment,
    List<String> imageUrls
) {}
