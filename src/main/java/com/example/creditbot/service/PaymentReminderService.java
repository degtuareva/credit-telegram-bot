package com.example.creditbot.service;

import com.example.creditbot.domain.PaymentReminder;
import com.example.creditbot.domain.ReminderStatus;
import com.example.creditbot.repository.PaymentReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentReminderService {

    private final PaymentReminderRepository reminderRepository;

    public PaymentReminderService(
            PaymentReminderRepository reminderRepository
    ) {
        this.reminderRepository = reminderRepository;
    }

    @Transactional
    public PaymentReminder createReminder(
            Long telegramUserId,
            Long creditRequestId,
            int daysBeforePayment
    ) {
        if (telegramUserId == null || telegramUserId <= 0) {
            throw new IllegalArgumentException(
                    "Некорректный идентификатор пользователя."
            );
        }

        if (daysBeforePayment < 1 || daysBeforePayment > 30) {
            throw new IllegalArgumentException(
                    "Напоминание можно установить на 1–30 дней."
            );
        }

        PaymentReminder reminder = new PaymentReminder(
                telegramUserId,
                creditRequestId,
                LocalDateTime.now().plusDays(daysBeforePayment)
        );

        return reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public List<PaymentReminder> getUserReminders(Long telegramUserId) {
        return reminderRepository
                .findByTelegramUserIdOrderByRemindAtDesc(telegramUserId);
    }

    @Transactional
    public void cancelReminder(Long telegramUserId, Long reminderId) {
        PaymentReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Напоминание не найдено."
                ));

        if (!reminder.getTelegramUserId().equals(telegramUserId)) {
            throw new IllegalArgumentException(
                    "Напоминание принадлежит другому пользователю."
            );
        }

        reminder.setStatus(ReminderStatus.CANCELLED);
        reminderRepository.save(reminder);
    }
}