package com.example.creditbot.bot.support;

import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Преобразует текст пользователя в параметры кредита.
 */
@Component
public class InputParser {

    public BigDecimal parseAmount(String text) {
        BigDecimal amount = parseDecimal(
                text,
                "Введите корректную сумму кредита."
        );

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Сумма кредита должна быть больше нуля."
            );
        }

        return amount;
    }

    public int parseTermMonths(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Введите срок кредита целым числом."
            );
        }

        final int months;

        try {
            months = Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Введите срок кредита целым числом."
            );
        }

        if (months < 1 || months > 360) {
            throw new IllegalArgumentException(
                    "Срок должен быть от 1 до 360 месяцев."
            );
        }

        return months;
    }

    public BigDecimal parseAnnualRate(String text) {
        BigDecimal rate = parseDecimal(
                text,
                "Введите корректную годовую процентную ставку."
        );

        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Процентная ставка не может быть отрицательной."
            );
        }

        return rate;
    }

    public PaymentType parsePaymentType(String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Выберите тип платежа с помощью кнопок."
            );
        }

        return switch (text.trim()) {
            case "1" -> PaymentType.ANNUITY;
            case "2" -> PaymentType.DIFFERENTIAL;
            default -> throw new IllegalArgumentException(
                    "Выберите тип платежа с помощью кнопок."
            );
        };
    }

    private BigDecimal parseDecimal(
            String text,
            String errorMessage
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }

        String normalized = text.trim().replace(",", ".");

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}