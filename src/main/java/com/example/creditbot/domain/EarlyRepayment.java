package com.example.creditbot.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "early_repayments")
public class EarlyRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "credit_request_id")
    private Long creditRequestId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "annual_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal annualRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "early_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal earlyPayment;

    @Column(name = "early_payment_month", nullable = false)
    private Integer earlyPaymentMonth;

    @Column(nullable = false)
    private String strategy;

    @Column(name = "interest_saved", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestSaved;

    @Column(name = "months_saved", nullable = false)
    private Integer monthsSaved;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public EarlyRepayment() {
    }

    public EarlyRepayment(
            Long telegramUserId,
            Long creditRequestId,
            BigDecimal amount,
            Integer termMonths,
            BigDecimal annualRate,
            PaymentType paymentType,
            BigDecimal earlyPayment,
            Integer earlyPaymentMonth,
            String strategy,
            BigDecimal interestSaved,
            Integer monthsSaved
    ) {
        this.telegramUserId = telegramUserId;
        this.creditRequestId = creditRequestId;
        this.amount = amount;
        this.termMonths = termMonths;
        this.annualRate = annualRate;
        this.paymentType = paymentType;
        this.earlyPayment = earlyPayment;
        this.earlyPaymentMonth = earlyPaymentMonth;
        this.strategy = strategy;
        this.interestSaved = interestSaved;
        this.monthsSaved = monthsSaved;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public Long getCreditRequestId() {
        return creditRequestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public BigDecimal getEarlyPayment() {
        return earlyPayment;
    }

    public Integer getEarlyPaymentMonth() {
        return earlyPaymentMonth;
    }

    public String getStrategy() {
        return strategy;
    }

    public BigDecimal getInterestSaved() {
        return interestSaved;
    }

    public Integer getMonthsSaved() {
        return monthsSaved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}