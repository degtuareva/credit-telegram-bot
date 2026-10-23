package com.example.creditbot.service;

import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.persistence.entity.CreditRequestEntity;
import com.example.creditbot.persistence.repository.CreditRequestJpaRepository;
import com.example.creditbot.web.dto.ManagerAnalyticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ManagerAnalyticsService {

    private final CreditRequestJpaRepository repository;

    public ManagerAnalyticsService(
            CreditRequestJpaRepository repository
    ) {
        this.repository = repository;
    }

    public ManagerAnalyticsResponse summarize(
            LocalDate from,
            LocalDate to
    ) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "Укажите корректный период"
            );
        }

        long days = ChronoUnit.DAYS.between(from, to) + 1;

        if (days > 366) {
            throw new IllegalArgumentException(
                    "Выберите период не больше 366 дней"
            );
        }

        List<CreditRequestEntity> requests =
                repository
                        .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
                                from.atStartOfDay(),
                                to.plusDays(1).atStartOfDay()
                        );

        Map<PaymentType, Long> paymentTypes =
                new EnumMap<>(PaymentType.class);

        for (PaymentType type : PaymentType.values()) {
            paymentTypes.put(type, 0L);
        }

        Map<LocalDate, Long> dailyCounts =
                new LinkedHashMap<>();

        for (LocalDate day = from;
             !day.isAfter(to);
             day = day.plusDays(1)) {

            dailyCounts.put(day, 0L);
        }

        BigDecimal amountSum = BigDecimal.ZERO;
        long termSum = 0;

        for (CreditRequestEntity request : requests) {
            amountSum = amountSum.add(request.getAmount());
            termSum += request.getTermMonths();

            paymentTypes.merge(
                    request.getPaymentType(),
                    1L,
                    Long::sum
            );

            dailyCounts.merge(
                    request.getCreatedAt().toLocalDate(),
                    1L,
                    Long::sum
            );
        }

        long count = requests.size();

        long uniqueUsers = requests.stream()
                .map(CreditRequestEntity::getTelegramUserId)
                .distinct()
                .count();

        BigDecimal averageAmount = count == 0
                ? BigDecimal.ZERO
                : amountSum.divide(
                BigDecimal.valueOf(count),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal averageTerm = count == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(termSum).divide(
                BigDecimal.valueOf(count),
                1,
                RoundingMode.HALF_UP
        );

        return new ManagerAnalyticsResponse(
                from,
                to,
                count,
                uniqueUsers,
                averageAmount,
                averageTerm,
                paymentTypes,
                dailyCounts
        );
    }
}