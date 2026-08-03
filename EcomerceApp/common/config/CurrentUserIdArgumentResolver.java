package com.tmt.ecommerce.common.config;

import com.tmt.ecommerce.identity.entity.User;
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

        // Lấy thẻ thông hành từ Két sắt Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // Đáng lý SecurityFilterChain đã chặn rồi, nhưng cứ phòng hờ cho chắc chắn
            throw new IllegalStateException("Không tìm thấy thông tin xác thực của người dùng.");
        }

        // Ép kiểu Principal về Entity User của chúng ta (vì ở Filter ta đã nạp Entity này vào)
        User user = (User) authentication.getPrincipal();

        // Trả về đúng ID
        return user.getId();
    }
}