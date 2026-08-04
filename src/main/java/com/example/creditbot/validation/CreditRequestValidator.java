package com.example.creditbot.validation;

import com.example.creditbot.domain.CreditRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditRequestValidator {

    public void validate(CreditRequest request) {
        if (request.amount() == null ||
                request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Сумма должна быть больше нуля"
            );
        }

        if (request.termMonths() <= 0 ||
                request.termMonths() > 360) {
            throw new IllegalArgumentException(
                    "Срок должен быть от 1 до 360 месяцев"
            );
        }

        if (request.annualRate() == null ||
                request.annualRate().compareTo(BigDecimal.ZERO) < 0 ||
                request.annualRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть от 0 до 100 процентов"
            );
        }

        if (request.paymentType() == null) {
            throw new IllegalArgumentException(
                    "Не указан тип платежа"
            );
        }
    }
}