package com.example.creditbot.web.dto;

import com.example.creditbot.domain.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Параметры расчёта досрочного погашения.
 */
public record EarlyRepaymentRequest(

        @NotNull(message = "Введите сумму кредита.")
        @DecimalMin(value = "100", message = "Минимальная сумма кредита — 100.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal amount,

        @NotNull(message = "Введите срок кредита.")
        @Min(value = 1, message = "Минимальный срок — 1 месяц.")
        @Max(value = 360, message = "Максимальный срок кредита — 360 месяцев.")
        Integer termMonths,

        @NotNull(message = "Введите годовую ставку.")
        @DecimalMin(value = "0", message = "Ставка не может быть отрицательной.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal annualRate,

        @NotNull(message = "Выберите тип платежа.")
        PaymentType earlyPaymentType,

        @NotNull(message = "Введите сумму досрочного платежа.")
        @DecimalMin(value = "1", message = "Сумма досрочного платежа должна быть больше нуля.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal earlyPayment,

        @NotNull(message = "Выберите месяц досрочного платежа.")
        @Min(value = 1, message = "Месяц должен быть не меньше 1.")
        @Max(value = 360, message = "Месяц должен быть не больше 360.")
        Integer earlyPaymentMonth,

        @NotNull(message = "Выберите стратегию погашения.")
        String strategy
) {
}