package com.srikanth.hellofsd.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GreetingRequest(
        @NotBlank(message = "author is required")
        @Size(max = 100, message = "author must be at most 100 characters")
        String author,

        @NotBlank(message = "message is required")
        @Size(max = 500, message = "message must be at most 500 characters")
        String message
) {
}
