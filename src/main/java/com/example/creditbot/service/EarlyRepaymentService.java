package com.example.creditbot.service;

import com.example.creditbot.calculator.EarlyRepaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculatorFactory;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.web.dto.EarlyRepaymentRequest;
import com.example.creditbot.web.dto.EarlyRepaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class EarlyRepaymentService {

    private static final int SCALE = 2;

    private final PaymentCalculatorFactory calculatorFactory;
    private final EarlyRepaymentCalculator earlyRepaymentCalculator;

    public EarlyRepaymentService(
            PaymentCalculatorFactory calculatorFactory,
            EarlyRepaymentCalculator earlyRepaymentCalculator
    ) {
        this.calculatorFactory = calculatorFactory;
        this.earlyRepaymentCalculator = earlyRepaymentCalculator;
    }

    public EarlyRepaymentResponse calculate(
            EarlyRepaymentRequest request
    ) {
        CreditRequest baseRequest = new CreditRequest(
                0L,
                0L,
                request.amount(),
                request.termMonths(),
                request.annualRate(),
                request.earlyPaymentType(),
                LocalDateTime.now()
        );

        CreditSchedule baseSchedule = calculate(baseRequest);

        CreditSchedule updatedSchedule =
                earlyRepaymentCalculator.calculateUpdatedSchedule(request);

        BigDecimal interestSaved = baseSchedule.totalInterest()
                .subtract(updatedSchedule.totalInterest())
                .setScale(SCALE, RoundingMode.HALF_UP);

        int monthsSaved = baseSchedule.payments().size()
                - updatedSchedule.payments().size();

        return new EarlyRepaymentResponse(
                baseSchedule,
                updatedSchedule,
                baseSchedule.totalInterest(),
                updatedSchedule.totalInterest(),
                interestSaved,
                baseSchedule.payments().size(),
                updatedSchedule.payments().size(),
                monthsSaved,
                monthlyPayment(baseSchedule),
                monthlyPayment(updatedSchedule),
                request.strategy()
        );
    }

    private CreditSchedule calculate(CreditRequest request) {
        PaymentCalculator calculator =
                calculatorFactory.getCalculator(request.paymentType());

        return calculator.calculate(request);
    }

    private BigDecimal monthlyPayment(CreditSchedule schedule) {
        return schedule.payments().stream()
                .findFirst()
                .map(Payment::totalPayment)
                .orElse(BigDecimal.ZERO);
    }
}