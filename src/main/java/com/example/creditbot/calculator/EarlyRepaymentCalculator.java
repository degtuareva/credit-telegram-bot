package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.web.dto.EarlyRepaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EarlyRepaymentCalculator {

    private static final String REDUCE_TERM = "REDUCE_TERM";
    private static final String REDUCE_PAYMENT = "REDUCE_PAYMENT";

    private final PaymentCalculatorFactory calculatorFactory;

    public EarlyRepaymentCalculator(
            PaymentCalculatorFactory calculatorFactory
    ) {
        this.calculatorFactory = calculatorFactory;
    }

    public CreditSchedule calculateUpdatedSchedule(
            EarlyRepaymentRequest request
    ) {
        CreditRequest baseRequest = toCreditRequest(request);

        CreditSchedule baseSchedule =
                calculatorFactory
                        .getCalculator(baseRequest.paymentType())
                        .calculate(baseRequest);

        List<Payment> payments = baseSchedule.payments();
        int earlyMonth = request.earlyPaymentMonth();

        if (earlyMonth < 1 || earlyMonth > payments.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Месяц досрочного платежа вне срока кредита."
            );
        }

        BigDecimal remainingDebt =
                payments.get(earlyMonth - 1).remainingDebt();

        BigDecimal newDebt = remainingDebt
                .subtract(request.earlyPayment());

        if (newDebt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Досрочный платёж превышает остаток долга."
            );
        }

        int monthsAfterEarlyPayment =
                payments.size() - earlyMonth;

        return switch (request.strategy()) {
            case REDUCE_TERM -> reduceTerm(
                    request,
                    newDebt,
                    monthsAfterEarlyPayment,
                    baseSchedule
            );

            case REDUCE_PAYMENT -> reducePayment(
                    request,
                    newDebt,
                    monthsAfterEarlyPayment
            );

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная стратегия досрочного погашения."
            );
        };
    }

    private CreditSchedule reduceTerm(
            EarlyRepaymentRequest request,
            BigDecimal newDebt,
            int monthsAfterEarlyPayment,
            CreditSchedule baseSchedule
    ) {
        BigDecimal monthlyPayment =
                baseSchedule.payments().get(0).totalPayment();

        CreditRequest updatedRequest = new CreditRequest(
                0L,
                0L,
                newDebt,
                monthsAfterEarlyPayment,
                request.annualRate(),
                request.earlyPaymentType(),
                LocalDateTime.now()
        );

        return calculateWithFixedPayment(
                updatedRequest,
                monthlyPayment
        );
    }

    private CreditSchedule reducePayment(
            EarlyRepaymentRequest request,
            BigDecimal newDebt,
            int monthsAfterEarlyPayment
    ) {
        CreditRequest updatedRequest = new CreditRequest(
                0L,
                0L,
                newDebt,
                monthsAfterEarlyPayment,
                request.annualRate(),
                request.earlyPaymentType(),
                LocalDateTime.now()
        );

        return calculatorFactory
                .getCalculator(updatedRequest.paymentType())
                .calculate(updatedRequest);
    }

    private CreditSchedule calculateWithFixedPayment(
            CreditRequest request,
            BigDecimal fixedPayment
    ) {
        BigDecimal monthlyRate = request.annualRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = request.amount();
        BigDecimal totalInterest = BigDecimal.ZERO;

        List<Payment> payments = new ArrayList<>();

        int month = 1;

        while (remainingDebt.compareTo(BigDecimal.ZERO) > 0
                && month <= 360) {

            BigDecimal interest = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = fixedPayment.subtract(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            if (principal.compareTo(remainingDebt) > 0) {
                principal = remainingDebt;
            }

            BigDecimal payment = principal.add(interest);

            remainingDebt = remainingDebt.subtract(principal)
                    .setScale(2, RoundingMode.HALF_UP);

            totalInterest = totalInterest.add(interest);

            payments.add(new Payment(
                    month,
                    principal,
                    interest,
                    payment,
                    remainingDebt.max(BigDecimal.ZERO)
            ));

            month++;
        }

        if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не удалось рассчитать график: платежа недостаточно."
            );
        }

        BigDecimal totalPayment = request.amount().add(totalInterest)
                .setScale(2, RoundingMode.HALF_UP);

        return new CreditSchedule(
                totalInterest.setScale(2, RoundingMode.HALF_UP),
                totalPayment,
                List.copyOf(payments)
        );
    }

    private CreditRequest toCreditRequest(
            EarlyRepaymentRequest request
    ) {
        return new CreditRequest(
                0L,
                0L,
                request.amount(),
                request.termMonths(),
                request.annualRate(),
                request.earlyPaymentType(),
                LocalDateTime.now()
        );
    }
}