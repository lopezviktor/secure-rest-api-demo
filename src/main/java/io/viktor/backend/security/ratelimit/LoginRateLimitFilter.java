package io.viktor.backend.security.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, this::createBucket);

            boolean allowed = bucket.tryConsume(1);

            long remainingAfter = bucket.getAvailableTokens();
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(MAX_REQUESTS));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remainingAfter));

            if (!allowed) {
                long nanosToWait = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
                long secondsToWait = Math.max(1, nanosToWait / 1_000_000_000);

                response.setHeader("Retry-After", String.valueOf(secondsToWait));
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                {
                  "error": "Too many login attempts. Please try again later."
                }
                """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "/auth/login".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private Bucket createBucket(String key) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(MAX_REQUESTS).refillGreedy(MAX_REQUESTS, WINDOW))
                .build();
    }
}