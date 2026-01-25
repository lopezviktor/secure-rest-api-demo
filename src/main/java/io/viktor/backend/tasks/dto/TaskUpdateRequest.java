package io.viktor.backend.tasks.dto;

public record TaskUpdateRequest(
        Boolean completed,
        String title
) {}
