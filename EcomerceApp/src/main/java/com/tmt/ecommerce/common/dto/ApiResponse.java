package com.tmt.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các field null khi trả về JSON
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data; // Dùng Generic (T) để chứa bất kỳ object nào (List User, 1 Shop, ...)
}