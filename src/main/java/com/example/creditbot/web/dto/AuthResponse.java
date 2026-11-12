package com.example.creditbot.web.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String firstName,
        boolean manager
) {
}