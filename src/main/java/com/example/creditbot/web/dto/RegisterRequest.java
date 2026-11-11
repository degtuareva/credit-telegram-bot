package com.example.creditbot.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Введите email.")
        @Email(message = "Некорректный email.")
        String email,

        @NotBlank(message = "Введите пароль.")
        @Size(min = 8, message = "Пароль должен содержать минимум 8 символов.")
        String password,

        @NotBlank(message = "Введите имя.")
        String firstName,

        String lastName
) {
}