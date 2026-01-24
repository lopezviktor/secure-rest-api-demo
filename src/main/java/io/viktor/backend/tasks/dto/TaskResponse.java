package io.viktor.backend.tasks.dto;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        boolean completed,
        Long userId,
        Instant createdAt
) {
}
