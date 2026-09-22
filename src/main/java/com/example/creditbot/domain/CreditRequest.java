package com.example.creditbot.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Модель кредитного запроса.
 * <p>
 * Содержит параметры кредита, введённые пользователем:
 * сумму, срок, процентную ставку и тип платежа.
 * </p>
 * <p>
 * Использует record для неизменяемости и автоматической генерации
 * методов getter, equals, hashCode и toString.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 */

public record CreditRequest(
        long id,
        long telegramUserId,
        BigDecimal amount,
        int termMonths,
        BigDecimal annualRate,
        PaymentType paymentType,
        LocalDateTime createdAt
) {
}
