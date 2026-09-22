package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис аналитики кредитных запросов.
 *
 * <p>Формирует агрегированную статистику
 * по типам платежей, срокам и суммам кредитов.</p>
 */
@Service
public class AnalyticsService {

    private final CreditRequestRepository repository;

    public AnalyticsService(
            CreditRequestRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Возвращает общее количество запросов.
     *
     * @return количество кредитных запросов
     */
    public long totalRequests() {
        return repository.findAll().size();
    }

    /**
     * Группирует запросы по типу платежа.
     *
     * @return статистика по типам платежей
     */
    public Map<PaymentType, Long> paymentTypeStats() {
        return repository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        CreditRequest::paymentType,
                        Collectors.counting()
                ));
    }

    /**
     * Группирует запросы по сроку кредита.
     *
     * @return статистика по срокам
     */
    public Map<Integer, Long> termStats() {
        return repository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        CreditRequest::termMonths,
                        Collectors.counting()
                ));
    }

    /**
     * Группирует запросы по сумме кредита.
     *
     * @return статистика по суммам
     */
    public Map<BigDecimal, Long> amountStats() {
        return repository.findAll()
                .stream()
                .map(CreditRequest::amount)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    /**
     * Фильтрует запросы по сумме и типу платежа.
     *
     * @param minAmount минимальная сумма
     * @param maxAmount максимальная сумма
     * @param paymentType тип платежа
     * @return список подходящих запросов
     */
    public List<CreditRequest> filter(
            BigDecimal minAmount,
            BigDecimal maxAmount,
            PaymentType paymentType
    ) {
        return repository.findAll()
                .stream()
                .filter(request ->
                        minAmount == null ||
                                request.amount()
                                        .compareTo(minAmount) >= 0
                )
                .filter(request ->
                        maxAmount == null ||
                                request.amount()
                                        .compareTo(maxAmount) <= 0
                )
                .filter(request ->
                        paymentType == null ||
                                request.paymentType() == paymentType
                )
                .toList();
    }
}