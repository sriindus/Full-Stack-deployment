package com.srikanth.hellofsd.web.dto;

import com.srikanth.hellofsd.domain.Greeting;

import java.time.Instant;

public record GreetingResponse(Long id, String author, String message, Instant createdAt) {

    public static GreetingResponse from(Greeting greeting) {
        return new GreetingResponse(
                greeting.getId(),
                greeting.getAuthor(),
                greeting.getMessage(),
                greeting.getCreatedAt()
        );
    }
}
