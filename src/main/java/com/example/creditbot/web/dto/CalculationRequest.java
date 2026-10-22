package com.example.creditbot.web.dto;

import com.example.creditbot.domain.PaymentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Параметры расчёта из Mini App.
 */
public record CalculationRequest(

        @NotNull(message = "Введите сумму кредита.")
        @DecimalMin(
                value = "0.01",
                message = "Сумма должна быть больше нуля."
        )
        @DecimalMax(
                value = "1000000000",
                message = "Сумма не должна превышать 1 000 000 000."
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Сумма должна содержать не более двух знаков после запятой."
        )
        BigDecimal amount,

        @NotNull(message = "Введите срок кредита.")
        @Min(value = 1, message = "Минимальный срок — 1 месяц.")
        @Max(value = 360, message = "Максимальный срок — 360 месяцев.")
        Integer termMonths,

        @NotNull(message = "Введите годовую ставку.")
        @DecimalMin(value = "0", message = "Ставка не может быть отрицательной.")
        @DecimalMax(value = "100", message = "Ставка не должна превышать 100%.")
        @Digits(
                integer = 3,
                fraction = 4,
                message = "Допустимо не более четырёх знаков после запятой."
        )
        BigDecimal annualRate,

        @NotNull(message = "Выберите тип платежа.")
        PaymentType paymentType
) {
}