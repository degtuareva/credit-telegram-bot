package com.example.creditbot.bot;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private final Map<Long, UserSession> sessions =
            new ConcurrentHashMap<>();

    public UserSession getSession(long userId) {
        return sessions.computeIfAbsent(
                userId,
                id -> new UserSession()
        );
    }

    public void clear(long userId) {
        sessions.remove(userId);
    }
}