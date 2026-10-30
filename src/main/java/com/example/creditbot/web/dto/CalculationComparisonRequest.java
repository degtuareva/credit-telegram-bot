package com.example.creditbot.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на сравнение двух вариантов кредита.
 */

public record CalculationComparisonRequest(
        @NotNull(message = "Передайте первый вариант кредита.")
        @Valid
        CalculationRequest first,

        @NotNull(message = "Передайте второй вариант кредита.")
        @Valid
        CalculationRequest second
) {

}
