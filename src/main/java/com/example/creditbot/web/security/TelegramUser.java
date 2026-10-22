package com.example.creditbot.web.security;

/**
 * Пользователь, извлечённый из проверенного initData.
 */
public record TelegramUser(
        long id,
        String firstName,
        String username
) {
}