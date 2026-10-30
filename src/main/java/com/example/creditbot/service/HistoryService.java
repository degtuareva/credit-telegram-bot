package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.repository.CreditRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Сервис получения истории запросов пользователя.
 */
@Service
public class HistoryService {

    private final CreditRequestRepository repository;

    public HistoryService(CreditRequestRepository repository) {
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

    /**
     * Удаляет расчёт текущего пользователя.
     *
     * @param telegramUserId Telegram ID пользователя
     * @param calculationId  ID расчёта
     */
    @Transactional
    public void deleteUserCalculation(
            Long telegramUserId,
            Long calculationId
    ) {

    }

    public void getUserCalculation(long id, Long id1) {
    }
}