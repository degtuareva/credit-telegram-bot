package com.example.creditbot.repository;

import com.example.creditbot.domain.PaymentReminder;
import com.example.creditbot.domain.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentReminderRepository
extends JpaRepository<PaymentReminder, Long> {
    List<PaymentReminder> findByStatusAndRemindAtLessThanEqual(
            ReminderStatus status,
            LocalDateTime remindAt
    );
    List<PaymentReminder> findByTelegramUserIdOrderByRemindAtDesc(
            Long telegramUserId
    );
}
