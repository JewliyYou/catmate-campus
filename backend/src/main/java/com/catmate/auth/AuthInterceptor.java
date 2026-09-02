package com.catmate.auth;

import com.catmate.user.UserAccount;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthSessionRepository sessions;
    private final ObjectMapper mapper;
    public AuthInterceptor(AuthSessionRepository sessions, ObjectMapper mapper) { this.sessions = sessions; this.mapper = mapper; }

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return reject(response, HttpStatus.UNAUTHORIZED, "请先登录");
        AuthSession session = sessions.findByTokenAndExpiresAtAfter(header.substring(7), LocalDateTime.now()).orElse(null);
        if (session == null) return reject(response, HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        UserAccount user = session.getUser();
        if (request.getRequestURI().startsWith("/api/admin") && user.getRole() != UserAccount.Role.ADMIN) return reject(response, HttpStatus.FORBIDDEN, "只有管理者可以访问后台");
        request.setAttribute("currentUser", user); return true;
    }

    private boolean reject(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value()); response.setContentType("application/json;charset=UTF-8"); mapper.writeValue(response.getWriter(), Map.of("message", message)); return false;
    }
}
