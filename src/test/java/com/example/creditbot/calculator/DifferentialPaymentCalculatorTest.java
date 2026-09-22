package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DifferentialPaymentCalculatorTest {

    private final DifferentialPaymentCalculator calculator =
            new DifferentialPaymentCalculator();

    @Test
    void shouldCalculateDifferentialSchedule() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(120_000),
                12,
                BigDecimal.valueOf(12),
                PaymentType.DIFFERENTIAL,
                LocalDateTime.now()
        );

        CreditSchedule schedule =
                calculator.calculate(request);

        assertEquals(
                12,
                schedule.payments().size()
        );

        assertTrue(
                schedule.payments()
                        .get(0)
                        .totalPayment()
                        .compareTo(
                                schedule.payments()
                                        .get(11)
                                        .totalPayment()
                        ) > 0
        );

        assertEquals(
                BigDecimal.ZERO.setScale(2),
                schedule.payments()
                        .getLast()
                        .remainingDebt()
        );
    }
}