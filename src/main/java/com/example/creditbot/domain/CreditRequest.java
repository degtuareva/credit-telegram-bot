package com.example.creditbot.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
