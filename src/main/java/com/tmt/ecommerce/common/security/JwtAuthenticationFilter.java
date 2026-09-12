package com.tmt.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.tmt.ecommerce.common.exception.ErrorCode;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Sẽ cấu hình ở bước sau
    private final RestSecurityExceptionHandler restSecurityExceptionHandler;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy chuỗi Authorization từ Header của HTTP Request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Kiểm tra xem Header có chứa Token chuẩn (bắt đầu bằng "Bearer ") không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Không có thẻ thì cho đi tiếp (để các API Public hoạt động)
            return;
        }

        // 3. Tách lấy phần Token (bỏ đi chữ "Bearer " - 7 ký tự)
        jwt = authHeader.substring(7);

        try {
            // 4. Gọi JwtService để giải mã lấy Email
        userEmail = jwtService.extractUsername(jwt);

        // 5. Nếu có Email và User này chưa được xác thực trong Context hiện tại
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Chui xuống Database kéo thông tin User lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Kiểm tra Token có còn hạn và có đúng của User này không
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Tạo một "Thẻ thông hành" (Authentication Token)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Đính kèm các thông tin của Request (như địa chỉ IP) vào thẻ
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. Cất thẻ vào SecurityContextHolder (Két sắt của Spring Security)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException e) {
            // Token is invalid or expired, just proceed without authentication
            // SecurityContext will remain null, leading to 401/403 for protected routes
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // Database/infrastructure failures must not masquerade as invalid credentials.
            SecurityContextHolder.clearContext();
            log.error("Authentication infrastructure failure", e);
            restSecurityExceptionHandler.write(response, ErrorCode.INTERNAL_SERVER_ERROR);
            return;
        }

        // 9. Cho phép Request đi tiếp tới Trạm gác tiếp theo hoặc tới Controller
        filterChain.doFilter(request, response);
    }
}
