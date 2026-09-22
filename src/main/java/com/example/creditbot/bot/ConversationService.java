package com.example.creditbot.bot;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет пользовательскими сессиями диалога.
 *
 * <p>Для каждого Telegram ID хранится отдельный объект
 * {@link UserSession}.</p>
 */
@Service
public class ConversationService {

    private final Map<Long, UserSession> sessions =
            new ConcurrentHashMap<>();

    /**
     * Возвращает существующую сессию или создаёт новую.
     *
     * @param userId идентификатор пользователя Telegram
     * @return сессия пользователя
     */
    public UserSession getSession(long userId) {
        return sessions.computeIfAbsent(
                userId,
                id -> new UserSession()
        );
    }

    /**
     * Удаляет сессию пользователя.
     *
     * @param userId идентификатор пользователя Telegram
     */
    public void clear(long userId) {
        sessions.remove(userId);
    }
}