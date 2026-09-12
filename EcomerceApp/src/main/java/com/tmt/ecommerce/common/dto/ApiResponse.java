package com.tmt.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmt.ecommerce.common.exception.ErrorCode;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String errorCode;
    private String message;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;
    private Map<String, String> fieldErrors;

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public static ApiResponse<Void> error(int status, ErrorCode code, String message) {
        return ApiResponse.<Void>builder().status(status).errorCode(code.name()).message(message).build();
    }
}
