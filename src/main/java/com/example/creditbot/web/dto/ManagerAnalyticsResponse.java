package com.example.creditbot.web.dto;

import com.example.creditbot.domain.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ManagerAnalyticsResponse(
        LocalDate from,
        LocalDate to,
        long totalRequests,
        long uniqueUsers,
        BigDecimal averageAmount,
        BigDecimal averageTermMonths,
        Map<PaymentType, Long> paymentTypes,
        Map<LocalDate, Long> dailyCounts
) {
}