package com.example.creditbot.support;

import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Проверяет и преобразует текст пользователя
 * в параметры кредитного расчёта.
 */
@Component
public class InputParser {

    private static final BigDecimal MIN_AMOUNT =
            new BigDecimal("10000");

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("50000000");

    private static final BigDecimal MIN_RATE =
            new BigDecimal("0.1");

    private static final BigDecimal MAX_RATE =
            new BigDecimal("100");

    public BigDecimal parseAmount(String text) {
        BigDecimal amount = parseDecimal(
                text,
                "сумму кредита"
        );

        if (amount.compareTo(MIN_AMOUNT) < 0
                || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(
                    "Сумма кредита должна быть от 10 000 до 50 000 000 рублей."
            );
        }

        return amount;
    }

    public int parseTermMonths(String text) {
        int termMonths;

        try {
            termMonths = Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Введите срок кредита целым числом."
            );
        }

        if (termMonths < 1 || termMonths > 360) {
            throw new IllegalArgumentException(
                    "Срок должен быть от 1 до 360 месяцев."
            );
        }

        return termMonths;
    }

    public BigDecimal parseAnnualRate(String text) {
        BigDecimal rate = parseDecimal(
                text,
                "годовую процентную ставку"
        );

        if (rate.compareTo(MIN_RATE) < 0
                || rate.compareTo(MAX_RATE) > 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть от 0.1% до 100%."
            );
        }

        return rate;
    }

    public PaymentType parsePaymentType(String callbackData) {
        return switch (callbackData.trim()) {
            case "1" -> PaymentType.ANNUITY;
            case "2" -> PaymentType.DIFFERENTIAL;
            default -> throw new IllegalArgumentException(
                    "Выберите тип платежа с помощью кнопок."
            );
        };
    }

    private BigDecimal parseDecimal(
            String text,
            String fieldName
    ) {
        try {
            return new BigDecimal(
                    text.replace(",", ".").trim()
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Введите корректную " + fieldName + "."
            );
        }
    }
}