package com.example.creditbot.domain;

import java.math.BigDecimal;

public record Payment(
        int month,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal totalPayment,
        BigDecimal remainingDebt
) {
}
