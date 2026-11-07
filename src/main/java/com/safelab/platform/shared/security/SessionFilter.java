package com.safelab.platform.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safelab.platform.shared.domain.ApiError;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.ApiMessageTranslator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

@Component
public class SessionFilter extends OncePerRequestFilter {
    private final MockSessionService auth;
    private final ObjectMapper mapper;
    private final ApiMessageTranslator translations;
    public SessionFilter(MockSessionService auth, ObjectMapper mapper, ApiMessageTranslator translations) { this.auth = auth; this.mapper = mapper; this.translations = translations; }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
               || path.equals("/api/v1/health") || path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register/company")
               || path.equals("/api/v1/auth/register/invitation") || path.equals("/error") || path.equals("/favicon.ico");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer "))
                throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Envía Authorization: Bearer <token>.");
            request.setAttribute("safelab.user", auth.byToken(header.substring(7)));
            chain.doFilter(request, response);
        } catch (ApiException ex) {
            response.setStatus(ex.status().value()); response.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(response.getWriter(), ApiError.of(ex.status().value(), ex.code(), translations.translate(ex.code(), ex.getMessage(), request), request.getRequestURI(), Map.of()));
        }
    }
}
