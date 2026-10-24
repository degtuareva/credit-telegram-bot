package com.example.creditbot.web.dto;

public record MiniAppResponse(
        Long id,
        String firstName,
        String userName,
        boolean manager,
        boolean managerAuthenticated
) {
}
