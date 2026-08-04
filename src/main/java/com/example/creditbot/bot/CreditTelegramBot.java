package com.example.creditbot.bot;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.service.CreditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CreditTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final String botToken;
    private final ConversationService conversationService;
    private final CreditService creditService;

    public CreditTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            ConversationService conversationService,
            CreditService creditService
    ) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.conversationService = conversationService;
        this.creditService = creditService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        UserSession session = conversationService.getSession(userId);

        try {
            if (text.startsWith("/")) {
                handleCommand(chatId, userId, text, session);
            } else {
                handleConversationStep(chatId, userId, text, session);
            }
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleCommand(long chatId, long userId, String text, UserSession session) {
        switch (text) {
            case "/start" -> sendMessage(chatId,
                    """
                            Привет! Я бот для расчёта графика погашения кредита.

                            Команды:
                            /calculate — начать расчёт
                            /history — история запросов
                            /help — помощь
                            """);

            case "/help" -> sendMessage(chatId,
                    """
                            Как пользоваться ботом:

                            1. Отправьте /calculate
                            2. Введите сумму кредита
                            3. Введите срок в месяцах
                            4. Введите годовую ставку
                            5. Выберите тип платежа
                            """);

            case "/calculate" -> {
                session.reset();
                session.setStep(ConversationStep.WAITING_AMOUNT);
                sendMessage(chatId, "Введите сумму кредита:");
            }

            case "/history" -> sendMessage(chatId, "История запросов пока не реализована.");

            default -> sendMessage(chatId, "Неизвестная команда. Используйте /help.");
        }
    }

    private void handleConversationStep(long chatId, long userId, String text, UserSession session) {
        switch (session.getStep()) {
            case WAITING_AMOUNT -> {
                BigDecimal amount = parsePositiveDecimal(text, "сумму кредита");
                session.setAmount(amount);
                session.setStep(ConversationStep.WAITING_TERM);
                sendMessage(chatId, "Введите срок кредита в месяцах:");
            }

            case WAITING_TERM -> {
                int termMonths = parsePositiveInt(text, "срок кредита");
                session.setTermMonths(termMonths);
                session.setStep(ConversationStep.WAITING_RATE);
                sendMessage(chatId, "Введите годовую процентную ставку:");
            }

            case WAITING_RATE -> {
                BigDecimal rate = parseNonNegativeDecimal(text, "процентную ставку");
                session.setAnnualRate(rate);
                session.setStep(ConversationStep.WAITING_PAYMENT_TYPE);
                sendMessage(chatId, """
                        Выберите тип платежа:
                        1 — аннуитетный
                        2 — дифференцированный
                        """);
            }

            case WAITING_PAYMENT_TYPE -> {
                PaymentType paymentType = parsePaymentType(text);
                session.setPaymentType(paymentType);

                CreditRequest request = new CreditRequest(
                        0L,
                        userId,
                        session.getAmount(),
                        session.getTermMonths(),
                        session.getAnnualRate(),
                        paymentType,
                        LocalDateTime.now()
                );

                CreditSchedule schedule = creditService.calculateAndSave(request);
                sendMessage(chatId, formatSchedule(schedule));
                conversationService.clear(userId);
            }

            case NONE -> sendMessage(chatId, "Сначала отправьте /calculate");
        }
    }

    private BigDecimal parsePositiveDecimal(String text, String fieldName) {
        try {
            BigDecimal value = new BigDecimal(text.replace(",", "."));
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(fieldName + " должно быть больше нуля");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите корректную " + fieldName);
        }
    }

    private BigDecimal parseNonNegativeDecimal(String text, String fieldName) {
        try {
            BigDecimal value = new BigDecimal(text.replace(",", "."));
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + " не может быть отрицательной");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите корректную " + fieldName);
        }
    }

    private int parsePositiveInt(String text, String fieldName) {
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new IllegalArgumentException(fieldName + " должно быть больше нуля");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите целое число для " + fieldName);
        }
    }

    private PaymentType parsePaymentType(String text) {
        return switch (text.trim()) {
            case "1" -> PaymentType.ANNUITY;
            case "2" -> PaymentType.DIFFERENTIAL;
            default -> throw new IllegalArgumentException("Введите 1 или 2");
        };
    }

    private String formatSchedule(CreditSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("График платежей\n");
        sb.append("Переплата: ").append(schedule.totalInterest()).append("\n");
        sb.append("Общая сумма выплат: ").append(schedule.totalPayment()).append("\n\n");

        for (Payment payment : schedule.payments()) {
            sb.append("Месяц ").append(payment.month())
                    .append(": ").append(payment.totalPayment())
                    .append(" руб. ")
                    .append("(тело: ").append(payment.principal())
                    .append(", проценты: ").append(payment.interest())
                    .append(", остаток: ").append(payment.remainingDebt())
                    .append(")\n");
        }

        return sb.toString();
    }

    private void sendMessage(long chatId, String text) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .build()
            );
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Не удалось отправить сообщение", e);
        }
    }
}