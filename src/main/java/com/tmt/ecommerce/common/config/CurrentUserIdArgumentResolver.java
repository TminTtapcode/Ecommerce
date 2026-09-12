package com.tmt.ecommerce.common.config;

import com.tmt.ecommerce.common.security.CurrentUserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.tmt.ecommerce.common.annotation.CurrentUserId;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    // 1. Dạy Spring: "Chỉ xử lý nếu tham số có gắn @CurrentUserId VÀ có kiểu là Long"
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    // 2. Logic trích xuất dữ liệu
    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        CurrentUserId annotation = parameter.getParameterAnnotation(CurrentUserId.class);
        boolean required = annotation == null || annotation.required();

        // Lấy thẻ thông hành từ Két sắt Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            if (!required) {
                return null;
            }
            // Đáng lý SecurityFilterChain đã chặn rồi, nhưng cứ phòng hờ cho chắc chắn
            throw new IllegalStateException("Không tìm thấy thông tin xác thực của người dùng.");
        }

        // Ép kiểu Principal về Interface CurrentUserPrincipal của Common
        if (authentication.getPrincipal() instanceof CurrentUserPrincipal principal) {
            return principal.getId();
        }

        if (!required) {
            return null;
        }

        throw new IllegalStateException("Không tìm thấy thông tin xác thực của người dùng.");
    }
}