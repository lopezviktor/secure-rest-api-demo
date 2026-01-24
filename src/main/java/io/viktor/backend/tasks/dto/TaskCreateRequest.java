package io.viktor.backend.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TaskCreateRequest(
        @NotBlank String title,
        @Positive Long userId
) {}
