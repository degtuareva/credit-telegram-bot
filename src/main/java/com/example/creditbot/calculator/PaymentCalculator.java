package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;

public interface PaymentCalculator {
    CreditSchedule calculate(CreditRequest request);
}
