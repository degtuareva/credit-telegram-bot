package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис получения истории запросов пользователя.
 */
@Service
public class HistoryService {

    private final CreditRequestRepository repository;

    public HistoryService(
            CreditRequestRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Возвращает историю кредитных запросов пользователя.
     *
     * @param userId Telegram ID пользователя
     * @return список запросов пользователя
     */
    public List<CreditRequest> getUserHistory(long userId) {
        return repository.findByTelegramUserId(userId);
    }
}