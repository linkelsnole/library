package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ReaderRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(max = 255, message = "Full name is too long")
        String fullName,

        String gender,

        @Min(value = 0, message = "Age must be non-negative")
        Integer age
) {}
