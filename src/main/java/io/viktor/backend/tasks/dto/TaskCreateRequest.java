package io.viktor.backend.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskCreateRequest(
        @NotBlank String title,
        @NotNull Long userId
) {
}
