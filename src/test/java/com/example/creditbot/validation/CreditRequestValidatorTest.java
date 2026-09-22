package com.example.creditbot.validation;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreditRequestValidatorTest {

    private final CreditRequestValidator validator =
            new CreditRequestValidator();

    @Test
    void shouldAcceptValidRequest() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(100_000),
                12,
                BigDecimal.valueOf(12),
                PaymentType.ANNUITY,
                LocalDateTime.now()
        );

        assertDoesNotThrow(
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(-100),
                12,
                BigDecimal.valueOf(12),
                PaymentType.ANNUITY,
                LocalDateTime.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldRejectInvalidTerm() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(100_000),
                500,
                BigDecimal.valueOf(12),
                PaymentType.ANNUITY,
                LocalDateTime.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldRejectInvalidRate() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(100_000),
                12,
                BigDecimal.valueOf(120),
                PaymentType.ANNUITY,
                LocalDateTime.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldRejectMissingPaymentType() {
        CreditRequest request = new CreditRequest(
                0L,
                123456789L,
                BigDecimal.valueOf(100_000),
                12,
                BigDecimal.valueOf(12),
                null,
                LocalDateTime.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }
}