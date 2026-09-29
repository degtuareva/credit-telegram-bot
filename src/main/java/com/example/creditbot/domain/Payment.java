package com.example.creditbot.domain;

import java.math.BigDecimal;

/**
 * Модель ежемесячного платежа.
 * <p>
 * Содержит детализацию платежа: номер месяца, сумму основного долга,
 * проценты, общий платёж и остаток задолженности.
 * </p>
 *
 * @author degtuareva
 * @version 1.0
 */

public record Payment(
        int month,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal totalPayment,
        BigDecimal remainingDebt
) {
}