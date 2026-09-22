package com.example.creditbot.validation;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Выполняет бизнес-валидацию кредитного запроса.
 *
 * <p>Класс проверяет Telegram ID пользователя, сумму,
 * срок, процентную ставку и тип платежа.</p>
 */
@Component
public class CreditRequestValidator {

    private static final int MIN_TERM_MONTHS = 1;
    private static final int MAX_TERM_MONTHS = 360;

    private static final BigDecimal MIN_AMOUNT =
            BigDecimal.valueOf(100);

    private static final BigDecimal MAX_AMOUNT =
            BigDecimal.valueOf(100_000_000);

    private static final BigDecimal MIN_RATE =
            BigDecimal.ZERO;

    private static final BigDecimal MAX_RATE =
            BigDecimal.valueOf(100);

    /**
     * Проверяет кредитный запрос.
     *
     * @param request проверяемый кредитный запрос
     * @throws IllegalArgumentException если данные некорректны
     */
    public void validate(CreditRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Кредитный запрос не может быть пустым"
            );
        }

        validateUserId(request.telegramUserId());
        validateAmount(request.amount());
        validateTerm(request.termMonths());
        validateRate(request.annualRate());
        validatePaymentType(request.paymentType());
    }

    private void validateUserId(long telegramUserId) {
        if (telegramUserId <= 0) {
            throw new IllegalArgumentException(
                    "Некорректный идентификатор пользователя"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Сумма кредита обязательна"
            );
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                    "Минимальная сумма кредита — 100"
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(
                    "Максимальная сумма кредита — 100000000"
            );
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Сумма может содержать не более двух знаков после запятой"
            );
        }
    }

    private void validateTerm(int termMonths) {
        if (termMonths < MIN_TERM_MONTHS ||
                termMonths > MAX_TERM_MONTHS) {
            throw new IllegalArgumentException(
                    "Срок должен быть от 1 до 360 месяцев"
            );
        }
    }

    private void validateRate(BigDecimal annualRate) {
        if (annualRate == null) {
            throw new IllegalArgumentException(
                    "Процентная ставка обязательна"
            );
        }

        if (annualRate.compareTo(MIN_RATE) < 0 ||
                annualRate.compareTo(MAX_RATE) > 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть от 0 до 100 процентов"
            );
        }

        if (annualRate.scale() > 2) {
            throw new IllegalArgumentException(
                    "Ставка может содержать не более двух знаков после запятой"
            );
        }
    }

    private void validatePaymentType(PaymentType paymentType) {
        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Не указан тип платежа"
            );
        }
    }
}