package com.example.creditbot.repository;

import com.example.creditbot.domain.CreditRequest;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с кредитными запросами.
 * <p>
 * Определяет абстракцию хранилища данных, позволяя заменять
 * реализацию (in-memory, база данных, файл) без изменения бизнес-логики.
 * </p>
 *
 * @author degtuareva
 * @version 1.0
 * @see CreditRequest
 * @see InMemoryCreditRequestRepository
 */
public interface CreditRequestRepository {
    /**
     * Сохраняет кредитный запрос в хранилище.
     *
     * @param request сохраняемый запрос
     * @return сохранённый запрос с присвоенным ID
     */

    CreditRequest save(CreditRequest request);

    /**
     * Находит все запросы по ID пользователя в Telegram.
     *
     * @param telegramUserId ID пользователя
     * @return список запросов пользователя
     */

    List<CreditRequest> findByTelegramUserId(long telegramUserId);

    /**
     * Находит все запросы в хранилище.
     *
     * @return список всех запросов
     */

    List<CreditRequest> findAll();

    /**
     * Находит запрос по уникальному ID.
     *
     * @param id уникальный идентификатор запроса
     * @return найденный запрос или пустой Optional
     */


    Optional<CreditRequest> findById(long id);
}