package com.example.creditbot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Введите email.")
        @Email(message = "Некорректный email.")
        String email,

        @NotBlank(message = "Введите пароль.")
        String password
) {
}