package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.web.dto.CalculationComparisonRequest;
import com.example.creditbot.web.dto.CalculationComparisonResponse;
import com.example.creditbot.web.dto.CalculationRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CalculationComparisonService {

    private static final int SCALE = 2;

    private final CreditService creditService;

    public CalculationComparisonService(CreditService creditService) {
        this.creditService = creditService;
    }

    public CalculationComparisonResponse compare(
            long telegramUserId,
            CalculationComparisonRequest request
    ) {
        CreditSchedule first =
                calculate(telegramUserId, request.first());

        CreditSchedule second =
                calculate(telegramUserId, request.second());

        BigDecimal firstMonthlyPayment = monthlyPayment(first);
        BigDecimal secondMonthlyPayment = monthlyPayment(second);

        BigDecimal firstTotalInterest = first.totalInterest();
        BigDecimal secondTotalInterest = second.totalInterest();

        BigDecimal firstTotalPayment = first.totalPayment();
        BigDecimal secondTotalPayment = second.totalPayment();

        BigDecimal monthlyPaymentDifference = secondMonthlyPayment
                .subtract(firstMonthlyPayment)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal totalInterestDifference = secondTotalInterest
                .subtract(firstTotalInterest)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal totalPaymentDifference = secondTotalPayment
                .subtract(firstTotalPayment)
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new CalculationComparisonResponse(
                first,
                second,
                firstMonthlyPayment,
                secondMonthlyPayment,
                firstTotalInterest,
                secondTotalInterest,
                firstTotalPayment,
                secondTotalPayment,
                monthlyPaymentDifference,
                totalInterestDifference,
                totalPaymentDifference,
                cheaperOption(firstTotalPayment, secondTotalPayment),
                cheaperOption(firstMonthlyPayment, secondMonthlyPayment),
                cheaperOption(firstTotalInterest, secondTotalInterest)
        );
    }

    private CreditSchedule calculate(
            long telegramUserId,
            CalculationRequest input
    ) {
        CreditRequest request = new CreditRequest(
                0L,
                telegramUserId,
                input.amount(),
                input.termMonths(),
                input.annualRate(),
                input.paymentType(),
                LocalDateTime.now()
        );

        return creditService.calculateWithoutSaving(request);
    }

    private BigDecimal monthlyPayment(CreditSchedule schedule) {
        return schedule.payments().stream()
                .findFirst()
                .map(payment -> payment.totalPayment())
                .orElse(BigDecimal.ZERO);
    }

    private String cheaperOption(
            BigDecimal firstValue,
            BigDecimal secondValue
    ) {
        int comparison = firstValue.compareTo(secondValue);

        if (comparison < 0) {
            return "FIRST";
        }

        if (comparison > 0) {
            return "SECOND";
        }

        return "EQUAL";
    }
}