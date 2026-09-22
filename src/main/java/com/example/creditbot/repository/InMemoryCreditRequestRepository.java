package com.example.creditbot.repository;

import com.example.creditbot.domain.CreditRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация репозитория для хранения запросов в памяти.
 * <p>
 * Использует ArrayList для хранения данных и ConcurrentHashMap
 * для управления сессиями пользователей. Подходит для учебных
 * и демонстрационных целей.
 * </p>
 * <p>
 * В production-среде может быть заменена на реализацию с
 * использованием базы данных (PostgreSQL, MySQL) без изменения
 * интерфейса.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 * @see CreditRequestRepository
 */

@Repository
@Profile("memory")
public class InMemoryCreditRequestRepository
        implements CreditRequestRepository {

    private final List<CreditRequest> requests = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public synchronized CreditRequest save(CreditRequest request) {
        CreditRequest saved = new CreditRequest(
                sequence.getAndIncrement(),
                request.telegramUserId(),
                request.amount(),
                request.termMonths(),
                request.annualRate(),
                request.paymentType(),
                request.createdAt()
        );

        requests.add(saved);
        return saved;
    }

    @Override
    public synchronized List<CreditRequest> findByTelegramUserId(
            long telegramUserId
    ) {
        return requests.stream()
                .filter(request ->
                        request.telegramUserId() == telegramUserId)
                .toList();
    }

    @Override
    public synchronized List<CreditRequest> findAll() {
        return List.copyOf(requests);
    }

    @Override
    public synchronized Optional<CreditRequest> findById(long id) {
        return requests.stream()
                .filter(request -> request.id() == id)
                .findFirst();
    }
}