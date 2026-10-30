package com.example.creditbot.service;

import com.example.creditbot.calculator.PaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculatorFactory;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.repository.CreditRequestRepository;
import com.example.creditbot.validation.CreditRequestValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис расчёта и сохранения кредитных запросов.
 */
@Service
public class CreditService {

    private final CreditRequestRepository repository;
    private final PaymentCalculatorFactory calculatorFactory;
    private final CreditRequestValidator validator;

    public CreditService(
            CreditRequestRepository repository,
            PaymentCalculatorFactory calculatorFactory,
            CreditRequestValidator validator
    ) {
        this.repository = repository;
        this.calculatorFactory = calculatorFactory;
        this.validator = validator;
    }

    /**
     * Валидирует запрос, сохраняет его и рассчитывает график.
     *
     * @param request параметры кредита
     * @return рассчитанный график платежей
     */
    public CreditSchedule calculateAndSave(CreditRequest request) {
        validator.validate(request);

        CreditRequest requestToSave = new CreditRequest(
                0L,
                request.telegramUserId(),
                request.amount(),
                request.termMonths(),
                request.annualRate(),
                request.paymentType(),
                LocalDateTime.now()
        );

        CreditRequest savedRequest = repository.save(requestToSave);

        return calculate(savedRequest);
    }

    /**
     * Рассчитывает график без сохранения запроса в историю.
     *
     * @param request параметры кредита
     * @return рассчитанный график платежей
     */
    public CreditSchedule calculateWithoutSaving(CreditRequest request) {
        validator.validate(request);

        return calculate(request);
    }

    private CreditSchedule calculate(CreditRequest request) {
        PaymentCalculator calculator =
                calculatorFactory.getCalculator(request.paymentType());

        return calculator.calculate(request);
    }
}