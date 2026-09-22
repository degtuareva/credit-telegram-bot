package com.example.creditbot.security;

import java.time.Instant;

/**
 * Хранит состояние авторизации менеджера.
 *
 * @param authenticated признак успешной авторизации
 * @param authenticatedAt время авторизации
 */
public record ManagerSession(
        boolean authenticated,
        Instant authenticatedAt
) {

    /**
     * Создаёт авторизованную сессию.
     *
     * @return новая авторизованная сессия
     */
    public static ManagerSession createAuthenticated() {
        return new ManagerSession(
                true,
                Instant.now()
        );
    }

    /**
     * Создаёт неавторизованную сессию.
     *
     * @return новая неавторизованная сессия
     */
    public static ManagerSession createUnauthenticate() {
        return new ManagerSession(
                false,
                null
        );
    }
}