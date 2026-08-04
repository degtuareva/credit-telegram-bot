package com.example.creditbot.bot;

import com.example.creditbot.domain.PaymentType;

import java.math.BigDecimal;

public class UserSession {

    private ConversationStep step = ConversationStep.NONE;
    private BigDecimal amount;
    private int termMonths;
    private BigDecimal annualRate;
    private PaymentType paymentType;

    public ConversationStep getStep() {
        return step;
    }

    public void setStep(ConversationStep step) {
        this.step = step;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
}