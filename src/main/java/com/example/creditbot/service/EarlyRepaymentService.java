package com.example.creditbot.service;

import com.example.creditbot.calculator.EarlyRepaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculator;
import com.example.creditbot.calculator.PaymentCalculatorFactory;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.EarlyRepayment;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.repository.EarlyRepaymentRepository;
import com.example.creditbot.web.dto.EarlyRepaymentRequest;
import com.example.creditbot.web.dto.EarlyRepaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EarlyRepaymentService {

    private static final int SCALE = 2;

    private final PaymentCalculatorFactory calculatorFactory;
    private final EarlyRepaymentCalculator earlyRepaymentCalculator;
    private final EarlyRepaymentRepository earlyRepaymentRepository;

    public EarlyRepaymentService(
            PaymentCalculatorFactory calculatorFactory,
            EarlyRepaymentCalculator earlyRepaymentCalculator,
            EarlyRepaymentRepository earlyRepaymentRepository
    ) {
        this.calculatorFactory = calculatorFactory;
        this.earlyRepaymentCalculator = earlyRepaymentCalculator;
        this.earlyRepaymentRepository = earlyRepaymentRepository;
    }

    @Transactional
    public EarlyRepaymentResponse calculate(
            Long telegramUserId,
            EarlyRepaymentRequest request
    ) {
        CreditRequest baseRequest = new CreditRequest(
                0L,
                telegramUserId,
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

        saveEarlyRepayment(
                telegramUserId,
                request,
                interestSaved,
                monthsSaved
        );

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

    @Transactional(readOnly = true)
    public List<EarlyRepayment> getUserEarlyRepayments(
            Long telegramUserId
    ) {
        return earlyRepaymentRepository
                .findByTelegramUserIdOrderByCreatedAtDesc(telegramUserId);
    }

    private void saveEarlyRepayment(
            Long telegramUserId,
            EarlyRepaymentRequest request,
            BigDecimal interestSaved,
            int monthsSaved
    ) {
        EarlyRepayment earlyRepayment = new EarlyRepayment(
                telegramUserId,
                null,
                request.amount(),
                request.termMonths(),
                request.annualRate(),
                request.earlyPaymentType(),
                request.earlyPayment(),
                request.earlyPaymentMonth(),
                request.strategy(),
                interestSaved,
                monthsSaved
        );

        earlyRepaymentRepository.save(earlyRepayment);
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