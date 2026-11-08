package com.example.creditbot.web.dto;

import com.example.creditbot.domain.CreditSchedule;

import java.math.BigDecimal;

/**
 * Результат расчёта досрочного погашения.
 */
public record EarlyRepaymentResponse(
        CreditSchedule baseSchedule,
        CreditSchedule updatedSchedule,

        BigDecimal baseTotalInterest,
        BigDecimal updatedTotalInterest,

        BigDecimal interestSaved,

        Integer baseTermMonths,
        Integer updatedTermMonths,

        Integer monthsSaved,

        BigDecimal baseMonthlyPayment,
        BigDecimal updatedMonthlyPayment,

        String strategy
) {
}