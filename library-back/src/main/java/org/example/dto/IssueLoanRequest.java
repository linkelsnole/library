package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record IssueLoanRequest(
        @NotNull(message = "bookId is required")
        Long bookId,

        @NotNull(message = "readerId is required")
        Long readerId
) {}
