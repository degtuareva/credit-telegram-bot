package com.example.creditbot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис авторизации менеджеров.
 *
 * <p>Проверяет Telegram ID пользователя, пароль менеджера
 * и хранит временные авторизованные сессии.</p>
 *
 * <p>Пароль и список разрешённых Telegram ID передаются
 * через конфигурацию или переменные окружения.</p>
 */
@Service
public class ManagerAuthService {

    private static final Duration SESSION_DURATION =
            Duration.ofMinutes(30);

    private final String managerPassword;
    private final String managerIds;

    private final Map<Long, ManagerSession> sessions =
            new ConcurrentHashMap<>();

    public ManagerAuthService(
            @Value("${telegram.manager-password}")
            String managerPassword,

            @Value("${telegram.manager-ids:}")
            String managerIds
    ) {
        if (managerPassword == null ||
                managerPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль менеджера не настроен"
            );
        }

        this.managerPassword = managerPassword;
        this.managerIds = managerIds;
    }

    /**
     * Проверяет, является ли пользователь разрешённым менеджером.
     *
     * @param userId Telegram ID пользователя
     * @return true, если ID есть в списке менеджеров
     */
    public boolean isAllowedManager(long userId) {
        if (managerIds == null ||
                managerIds.isBlank()) {
            return false;
        }

        return Arrays.stream(managerIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .anyMatch(id -> {
                    try {
                        return Long.parseLong(id) == userId;
                    } catch (NumberFormatException exception) {
                        return false;
                    }
                });
    }

    /**
     * Проверяет пароль и создаёт авторизованную сессию.
     *
     * @param userId Telegram ID пользователя
     * @param password введённый пароль
     * @return true, если авторизация выполнена успешно
     */
    public boolean authenticate(
            long userId,
            String password
    ) {
        if (!isAllowedManager(userId)) {
            return false;
        }

        if (password == null ||
                !managerPassword.equals(password.trim())) {
            return false;
        }

        sessions.put(
                userId,
                ManagerSession.createAuthenticated()
        );

        return true;
    }

    /**
     * Проверяет наличие действующей авторизованной сессии.
     *
     * @param userId Telegram ID пользователя
     * @return true, если пользователь авторизован
     */
    public boolean isAuthenticated(long userId) {
        ManagerSession session =
                sessions.get(userId);

        if (session == null ||
                !session.authenticated()) {
            return false;
        }

        if (session.authenticatedAt() == null) {
            sessions.remove(userId);
            return false;
        }

        boolean expired =
                session.authenticatedAt()
                        .plus(SESSION_DURATION)
                        .isBefore(Instant.now());

        if (expired) {
            sessions.remove(userId);
            return false;
        }

        return true;
    }

    /**
     * Завершает сессию менеджера.
     *
     * @param userId Telegram ID пользователя
     */
    public void logout(long userId) {
        sessions.remove(userId);
    }
}