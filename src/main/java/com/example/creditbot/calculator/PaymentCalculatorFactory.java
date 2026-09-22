package com.example.creditbot.calculator;

import com.example.creditbot.domain.PaymentType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Фабрика для создания калькуляторов платежей.
 * <p>
 * Реализует паттерн Factory Method, предоставляя клиентскому коду
 * возможность получения нужного калькулятора по типу платежа
 * без знания деталей реализации.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 * @see PaymentCalculator
 * @see PaymentType
 */

@Component
public class PaymentCalculatorFactory {

    private final Map<PaymentType, PaymentCalculator> calculators;

    public PaymentCalculatorFactory(
            AnnuityPaymentCalculator annuityCalculator,
            DifferentialPaymentCalculator differentialCalculator
    ) {
        this.calculators = Map.of(
                PaymentType.ANNUITY, annuityCalculator,
                PaymentType.DIFFERENTIAL, differentialCalculator
        );
    }

    public PaymentCalculator getCalculator(PaymentType type) {
        PaymentCalculator calculator = calculators.get(type);

        if (calculator == null) {
            throw new IllegalArgumentException(
                    "Неизвестный тип платежа: " + type
            );
        }

        return calculator;
    }
}