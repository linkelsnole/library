package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record BookRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title is too long")
        String title,

        @NotBlank(message = "Author cannot be blank")
        String author,

        Integer year,
        String isbn
) {}
