package com.example.creditbot.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reminders")
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "credit_request_id")
    private Long creditRequestId;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PaymentReminder() {
    }

    public PaymentReminder(
            Long telegramUserId,
            Long creditRequestId,
            LocalDateTime remindAt
    ) {
        this.telegramUserId = telegramUserId;
        this.creditRequestId = creditRequestId;
        this.remindAt = remindAt;
        this.status = ReminderStatus.PENDING;
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

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}