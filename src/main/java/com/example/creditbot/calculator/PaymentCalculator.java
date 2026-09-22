package com.example.creditbot.calculator;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;

/**
 * Интерфейс для калькуляторов графиков платежей.
 * <p>
 * Определяет контракт для всех реализаций калькуляторов,
 * обеспечивая возможность переключения между разными схемами расчёта
 * (аннуитетный, дифференцированный) без изменения клиентского кода.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 * @see AnnuityPaymentCalculator
 * @see DifferentialPaymentCalculator
 */
public interface PaymentCalculator {
    /**
     * Рассчитывает график погашения кредита.
     *
     * @param request параметры кредитного запроса
     * @return рассчитанный график платежей
     */
    CreditSchedule calculate(CreditRequest request);
}
