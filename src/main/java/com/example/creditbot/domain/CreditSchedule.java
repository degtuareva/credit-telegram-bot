package com.example.creditbot.domain;

import java.math.BigDecimal;
import java.util.List;

public record CreditSchedule(
        BigDecimal totalInterest,
        BigDecimal totalPayment,
        List<Payment> payments
) {
}
