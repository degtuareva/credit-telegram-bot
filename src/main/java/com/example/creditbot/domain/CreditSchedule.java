package com.example.creditbot.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Модель графика погашения кредита.
 * <p>
 * Содержит рассчитанные данные: общую переплату, сумму выплат
 * и помесячный список платежей.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 * @see Payment
 */
public record CreditSchedule(
        BigDecimal totalInterest,
        BigDecimal totalPayment,
        List<Payment> payments
) {
}
