package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnuityPaymentCalculatorTest {

    private final AnnuityPaymentCalculator calculator =
            new AnnuityPaymentCalculator();

    @Test
    void shouldCalculateAnnuitySchedule() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(100_000),
                12,
                BigDecimal.valueOf(12),
                PaymentType.ANNUITY,
                LocalDateTime.now()
        );

        CreditSchedule schedule =
                calculator.calculate(request);

        assertEquals(
                12,
                schedule.payments().size()
        );

        assertTrue(
                schedule.totalPayment()
                        .compareTo(BigDecimal.valueOf(100_000)) > 0
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                schedule.payments()
                        .getLast()
                        .remainingDebt()
        );
    }
}