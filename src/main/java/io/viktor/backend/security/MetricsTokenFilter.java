package io.viktor.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class MetricsTokenFilter extends OncePerRequestFilter {

    private final String metricsToken;

    public MetricsTokenFilter(@Value("${management.metrics.token:}") String metricsToken) {
        this.metricsToken = metricsToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isPrometheusRequest(request)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String authHeader = request.getHeader("Authorization");
            String token = extractBearerToken(authHeader);

            if (token != null && !metricsToken.isBlank() && token.equals(metricsToken)) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                var auth = new UsernamePasswordAuthenticationToken("prometheus", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPrometheusRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && "/actuator/prometheus".equals(request.getRequestURI());
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7).trim();
    }
}