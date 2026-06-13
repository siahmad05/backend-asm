package com.example.demo.config;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DuxWebApiTokenFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return !requestUri.startsWith("/duxweb/") || "/duxweb/token".equals(requestUri);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing bearer token");
            return;
        }

        String accessToken = authorization.substring("Bearer ".length());
        if (accessToken.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing bearer token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
