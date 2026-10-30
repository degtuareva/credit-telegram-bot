package com.example.creditbot.web.dto;

import com.example.creditbot.domain.CreditSchedule;

import java.math.BigDecimal;

/**
 * Результат сравнения двух вариантов кредита.
 */
public record CalculationComparisonResponse(
        CreditSchedule first,
        CreditSchedule second,

        BigDecimal firstMonthlyPayment,
        BigDecimal secondMonthlyPayment,

        BigDecimal firstTotalInterest,
        BigDecimal secondTotalInterest,

        BigDecimal firstTotalPayment,
        BigDecimal  secondTotalPayment,

        BigDecimal monthlyPaymentDifference,
        BigDecimal totalInterestDifference,
        BigDecimal totalPaymentDifference,

        String cheaperByTotalPayment,
        String lowerMonthlyPayment,
        String lowerTotalInterest
) {
}
