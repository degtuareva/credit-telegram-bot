package com.example.creditbot.service;

import com.example.creditbot.bot.support.TelegramMessageSender;
import com.example.creditbot.domain.PaymentReminder;
import com.example.creditbot.domain.ReminderStatus;
import com.example.creditbot.repository.PaymentReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ReminderScheduler.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final PaymentReminderRepository reminderRepository;
    private final TelegramMessageSender messageSender;

    public ReminderScheduler(
            PaymentReminderRepository reminderRepository,
            TelegramMessageSender messageSender
    ) {
        this.reminderRepository = reminderRepository;
        this.messageSender = messageSender;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendDueReminders() {
        List<PaymentReminder> reminders =
                reminderRepository.findByStatusAndRemindAtLessThanEqual(
                        ReminderStatus.PENDING,
                        LocalDateTime.now()
                );

        for (PaymentReminder reminder : reminders) {
            String text = """
                    🔔 Напоминание о платеже

                    Время напоминания: %s

                    Не забудьте проверить график платежей
                    в кредитном помощнике.
                    """.formatted(
                    reminder.getRemindAt()
                            .format(DATE_TIME_FORMATTER)
            );

            try {
                messageSender.send(
                        reminder.getTelegramUserId(),
                        text
                );

                reminder.setStatus(ReminderStatus.SENT);
                reminderRepository.save(reminder);

                log.info(
                        "Отправлено напоминание {} пользователю {}",
                        reminder.getId(),
                        reminder.getTelegramUserId()
                );

            } catch (RuntimeException exception) {
                log.error(
                        "Не удалось отправить напоминание {} пользователю {}",
                        reminder.getId(),
                        reminder.getTelegramUserId(),
                        exception
                );
            }
        }
    }
}