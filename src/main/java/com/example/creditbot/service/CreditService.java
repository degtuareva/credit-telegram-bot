package com.example.creditbot.service;

import com.example.creditbot.calculator.PaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculatorFactory;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreditService {

    private final CreditRequestRepository repository;
    private final PaymentCalculatorFactory calculatorFactory;

    public CreditService(
            CreditRequestRepository repository,
            PaymentCalculatorFactory calculatorFactory
    ) {
        this.repository = repository;
        this.calculatorFactory = calculatorFactory;
    }

    public CreditSchedule calculateAndSave(CreditRequest request) {
        CreditRequest savedRequest = repository.save(
                new CreditRequest(
                        0,
                        request.telegramUserId(),
                        request.amount(),
                        request.termMonths(),
                        request.annualRate(),
                        request.paymentType(),
                        LocalDateTime.now()
                )
        );

        PaymentCalculator calculator =
                calculatorFactory.getCalculator(
                        savedRequest.paymentType()
                );

        return calculator.calculate(savedRequest);
    }
}