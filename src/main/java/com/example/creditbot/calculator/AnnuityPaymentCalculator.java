package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Калькулятор аннуитетных платежей.
 * <p>
 * Рассчитывает график погашения кредита по аннуитетной схеме,
 * где ежемесячный платёж остаётся постоянным на всём сроке.
 * </p>
 * <p>
 * Использует формулу:
 * A = S × (i × (1+i)^n) / ((1+i)^n - 1)
 * где S — сумма кредита, i — месячная ставка, n — срок в месяцах.
 * </p>
 *
 * @author degtuareva
 * @version 1.0
 * @see PaymentCalculator
 */

@Component
public class AnnuityPaymentCalculator implements PaymentCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Override
    public CreditSchedule calculate(CreditRequest request) {
        BigDecimal principal = request.amount();
        BigDecimal monthlyRate = request.annualRate()
                .divide(BigDecimal.valueOf(12 * 100L), 12, ROUNDING);

        int months = request.termMonths();

        BigDecimal payment;

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            payment = principal.divide(
                    BigDecimal.valueOf(months), 12, ROUNDING
            );
        } else {
            double rate = monthlyRate.doubleValue();

            double coefficient = rate * Math.pow(1 + rate, months)
                    / (Math.pow(1 + rate, months) - 1);

            payment = principal
                    .multiply(BigDecimal.valueOf(coefficient));
        }

        payment = money(payment);

        List<Payment> payments = new ArrayList<>();
        BigDecimal remainingDebt = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= months; month++) {
            BigDecimal interest = money(remainingDebt.multiply(monthlyRate));
            BigDecimal principalPart = money(payment.subtract(interest));

            if (month == months) {
                principalPart = remainingDebt;
                payment = money(principalPart.add(interest));
            }

            remainingDebt = money(remainingDebt.subtract(principalPart));
            totalInterest = totalInterest.add(interest);

            payments.add(new Payment(
                    month,
                    principalPart,
                    interest,
                    payment,
                    remainingDebt
            ));
        }

        return new CreditSchedule(
                money(totalInterest),
                money(payments.stream()
                        .map(Payment::totalPayment)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)),
                payments
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING);
    }
}