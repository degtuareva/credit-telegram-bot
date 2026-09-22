package com.example.creditbot.bot;

import com.example.creditbot.domain.PaymentType;

import java.math.BigDecimal;

/**
 * Хранит состояние диалога конкретного пользователя.
 *
 * <p>Объект содержит введённые параметры кредита до момента
 * формирования полного {@code CreditRequest}.</p>
 */
public class UserSession {

    private ConversationStep step =
            ConversationStep.NONE;

    private BigDecimal amount;
    private int termMonths;
    private BigDecimal annualRate;
    private PaymentType paymentType;

    /**
     * Возвращает текущий шаг диалога.
     *
     * @return текущий шаг
     */
    public ConversationStep getStep() {
        return step;
    }

    /**
     * Изменяет текущий шаг диалога.
     *
     * @param step новый шаг
     */
    public void setStep(ConversationStep step) {
        this.step = step;
    }

    /**
     * Возвращает сумму кредита.
     *
     * @return сумма кредита
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Сохраняет сумму кредита.
     *
     * @param amount сумма кредита
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Возвращает срок кредита.
     *
     * @return срок в месяцах
     */
    public int getTermMonths() {
        return termMonths;
    }

    /**
     * Сохраняет срок кредита.
     *
     * @param termMonths срок в месяцах
     */
    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    /**
     * Возвращает годовую ставку.
     *
     * @return годовая процентная ставка
     */
    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    /**
     * Сохраняет годовую ставку.
     *
     * @param annualRate годовая процентная ставка
     */
    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    /**
     * Возвращает тип платежа.
     *
     * @return тип платежа
     */
    public PaymentType getPaymentType() {
        return paymentType;
    }

    /**
     * Сохраняет тип платежа.
     *
     * @param paymentType тип платежа
     */
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * Очищает текущую сессию и возвращает её
     * в исходное состояние.
     */
    public void reset() {
        step = ConversationStep.NONE;
        amount = null;
        termMonths = 0;
        annualRate = null;
        paymentType = null;
    }
}