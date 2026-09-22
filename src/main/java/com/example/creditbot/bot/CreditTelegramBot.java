package com.example.creditbot.bot;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.Payment;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.AnalyticsService;
import com.example.creditbot.service.CreditService;
import com.example.creditbot.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Telegram-бот для расчёта графиков погашения кредитов.
 *
 * <p>Класс принимает команды и сообщения пользователей,
 * управляет пошаговым диалогом, вызывает сервисы приложения
 * и отправляет результаты обратно в Telegram.</p>
 *
 * <p>Класс является транспортным слоем. Расчёты, валидация,
 * история и аналитика выполняются в отдельных сервисах.</p>
 *
 * @author Ваше имя
 * @version 1.0
 */
@Component
@SuppressWarnings("deprecation")
public class CreditTelegramBot
        implements SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(CreditTelegramBot.class);

    private static final int TELEGRAM_MESSAGE_LIMIT = 4000;

    private final ManagerAuthService managerAuthService;
    private final TelegramClient telegramClient;
    private final String botToken;
    private final String managerIds;
    private final ConversationService conversationService;
    private final CreditService creditService;
    private final HistoryService historyService;
    private final AnalyticsService analyticsService;

    public CreditTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.manager-ids:}") String managerIds,
            ConversationService conversationService,
            CreditService creditService,
            HistoryService historyService,
            AnalyticsService analyticsService,
            ManagerAuthService managerAuthService
    ) {
        this.managerAuthService = managerAuthService;
        this.botToken = botToken;
        this.managerIds = managerIds;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.conversationService = conversationService;
        this.creditService = creditService;
        this.historyService = historyService;
        this.analyticsService = analyticsService;
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
        if (update == null ||
                !update.hasMessage() ||
                !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        UserSession session =
                conversationService.getSession(userId);

        try {
            if (text.startsWith("/")) {
                handleCommand(chatId, userId, text, session);
            } else {
                handleConversationStep(
                        chatId,
                        userId,
                        text,
                        session
                );
            }
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Ошибка ввода от пользователя {}: {}",
                    userId,
                    exception.getMessage()
            );

            sendMessage(
                    chatId,
                    "Ошибка ввода: " + exception.getMessage()
            );
        } catch (Exception exception) {
            log.error(
                    "Ошибка обработки сообщения пользователя {}",
                    userId,
                    exception
            );

            sendMessage(
                    chatId,
                    "Произошла внутренняя ошибка. Попробуйте позже."
            );
        }
    }

    private void handleCommand(
            long chatId,
            long userId,
            String text,
            UserSession session
    ) {
        String command = extractCommand(text);

        switch (command) {
            case "/start" -> sendMessage(
                    chatId,
                    """
                    Привет! Я бот для расчёта графика погашения кредита.

                    Команды:
                    /calculate — начать расчёт
                    /history — история запросов
                    /analytics — аналитика для менеджера
                    /help — помощь
                    """
            );

            case "/help" -> sendMessage(
                    chatId,
                    """
                    Как пользоваться ботом:

                    1. Отправьте /calculate.
                    2. Введите сумму кредита.
                    3. Введите срок в месяцах.
                    4. Введите годовую ставку.
                    5. Выберите тип платежа.
                    """
            );

            case "/calculate" -> {
                session.reset();
                session.setStep(
                        ConversationStep.WAITING_AMOUNT
                );

                sendMessage(
                        chatId,
                        "Введите сумму кредита:"
                );
            }

            case "/history" -> sendMessage(
                    chatId,
                    formatHistory(userId)
            );
            case "/manager_login" -> {
                if (!managerAuthService.isAllowedManager(userId)) {
                    sendMessage(
                            chatId,
                            "Доступ запрещён. Вы не являетесь менеджером."
                    );
                    return;
                }

                session.reset();
                session.setStep(
                        ConversationStep.WAITING_MANAGER_PASSWORD
                );

                sendMessage(
                        chatId,
                        "Введите пароль менеджера:"
                );
            }

            case "/manager_logout" -> {
                managerAuthService.logout(userId);
                sendMessage(
                        chatId,
                        "Вы вышли из менеджерской сессии."
                );
            }

            case "/analytics" -> {
                if (!managerAuthService.isAuthenticated(userId)) {
                    sendMessage(
                            chatId,
                            """
                            Доступ запрещён.
            
                            Для входа используйте:
                            /manager_login
                            """
                    );
                    return;
                }

                sendMessage(
                        chatId,
                        formatAnalytics()
                );
            }

            default -> sendMessage(
                    chatId,
                    "Неизвестная команда. Используйте /help."
            );
        }
    }

    private String extractCommand(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String command = text
                .trim()
                .split("\\s+", 2)[0]
                .toLowerCase();

        int separatorIndex = command.indexOf('@');

        if (separatorIndex > 0) {
            command = command.substring(0, separatorIndex);
        }

        return command;
    }

    private void handleConversationStep(
            long chatId,
            long userId,
            String text,
            UserSession session
    ) {
        switch (session.getStep()) {
            case WAITING_AMOUNT -> {
                BigDecimal amount =
                        parsePositiveDecimal(
                                text,
                                "сумма кредита"
                        );

                session.setAmount(amount);
                session.setStep(
                        ConversationStep.WAITING_TERM
                );

                sendMessage(
                        chatId,
                        "Введите срок кредита в месяцах:"
                );
            }

            case WAITING_TERM -> {
                int termMonths =
                        parseTermMonths(text);

                session.setTermMonths(termMonths);
                session.setStep(
                        ConversationStep.WAITING_RATE
                );

                sendMessage(
                        chatId,
                        "Введите годовую процентную ставку:"
                );
            }

            case WAITING_RATE -> {
                BigDecimal rate =
                        parseNonNegativeDecimal(
                                text,
                                "процентная ставка"
                        );

                session.setAnnualRate(rate);
                session.setStep(
                        ConversationStep.WAITING_PAYMENT_TYPE
                );

                sendMessage(
                        chatId,
                        """
                        Выберите тип платежа:

                        1 — аннуитетный
                        2 — дифференцированный
                        """
                );
            }
            case WAITING_MANAGER_PASSWORD -> {
                boolean authenticated =
                        managerAuthService.authenticate(
                                userId,
                                text
                        );

                session.reset();
                conversationService.clear(userId);

                if (authenticated) {
                    sendMessage(
                            chatId,
                            """
                            Авторизация успешно выполнена.
            
                            Теперь доступна команда:
                            /analytics
                            """
                    );
                } else {
                    sendMessage(
                            chatId,
                            "Неверный пароль."
                    );
                }
            }

            case WAITING_PAYMENT_TYPE -> {
                PaymentType paymentType =
                        parsePaymentType(text);

                session.setPaymentType(paymentType);

                CreditRequest request = new CreditRequest(
                        0L,
                        userId,
                        session.getAmount(),
                        session.getTermMonths(),
                        session.getAnnualRate(),
                        session.getPaymentType(),
                        LocalDateTime.now()
                );

                CreditSchedule schedule =
                        creditService.calculateAndSave(request);

                sendSchedule(chatId, schedule);
                conversationService.clear(userId);
            }

            case NONE -> sendMessage(
                    chatId,
                    "Сначала отправьте /calculate."
            );
        }
    }

    private String formatHistory(long userId) {
        List<CreditRequest> history =
                historyService.getUserHistory(userId);

        if (history.isEmpty()) {
            return "У вас пока нет сохранённых запросов.";
        }

        StringBuilder result =
                new StringBuilder("История запросов:\n\n");

        for (int index = 0; index < history.size(); index++) {
            CreditRequest request = history.get(index);

            result.append(index + 1)
                    .append(". ")
                    .append(request.amount())
                    .append(" руб., срок: ")
                    .append(request.termMonths())
                    .append(" мес., ставка: ")
                    .append(request.annualRate())
                    .append("%, тип: ")
                    .append(formatPaymentType(request.paymentType()))
                    .append(", дата: ")
                    .append(request.createdAt())
                    .append("\n");
        }

        return result.toString();
    }

    private String formatAnalytics() {
        long totalRequests =
                analyticsService.totalRequests();

        Map<PaymentType, Long> paymentTypes =
                analyticsService.paymentTypeStats();

        Map<Integer, Long> terms =
                analyticsService.termStats();

        String popularTerm = terms.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry ->
                        entry.getKey() + " мес. (" +
                                entry.getValue() +
                                " запросов)"
                )
                .orElse("нет данных");

        return """
                Аналитика по кредитным запросам:

                Всего запросов: %d

                Популярный срок: %s

                Аннуитетный тип: %d
                Дифференцированный тип: %d
                """.formatted(
                totalRequests,
                popularTerm,
                paymentTypes.getOrDefault(
                        PaymentType.ANNUITY,
                        0L
                ),
                paymentTypes.getOrDefault(
                        PaymentType.DIFFERENTIAL,
                        0L
                )
        );
    }

    private BigDecimal parsePositiveDecimal(
            String text,
            String fieldName
    ) {
        BigDecimal value =
                parseDecimal(text, fieldName);

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " должна быть больше нуля."
            );
        }

        return value;
    }

    private BigDecimal parseNonNegativeDecimal(
            String text,
            String fieldName
    ) {
        BigDecimal value =
                parseDecimal(text, fieldName);

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " не может быть отрицательной."
            );
        }

        return value;
    }

    private BigDecimal parseDecimal(
            String text,
            String fieldName
    ) {
        String normalizedText =
                text.replace(",", ".").trim();

        try {
            return new BigDecimal(normalizedText);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Введите корректную " + fieldName + "."
            );
        }
    }

    private int parseTermMonths(String text) {
        int termMonths =
                parseInteger(text, "срок кредита");

        if (termMonths < 1 || termMonths > 360) {
            throw new IllegalArgumentException(
                    "Срок должен быть от 1 до 360 месяцев."
            );
        }

        return termMonths;
    }

    private int parseInteger(
            String text,
            String fieldName
    ) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Введите целое число: " + fieldName + "."
            );
        }
    }

    private PaymentType parsePaymentType(String text) {
        return switch (text.trim()) {
            case "1" -> PaymentType.ANNUITY;
            case "2" -> PaymentType.DIFFERENTIAL;
            default -> throw new IllegalArgumentException(
                    "Введите 1 для аннуитетного или " +
                            "2 для дифференцированного платежа."
            );
        };
    }

    private boolean isManager(long userId) {
        if (managerIds == null || managerIds.isBlank()) {
            return false;
        }

        return Arrays.stream(managerIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .anyMatch(id -> {
                    try {
                        return Long.parseLong(id) == userId;
                    } catch (NumberFormatException exception) {
                        log.warn(
                                "Некорректный manager ID в конфигурации: {}",
                                id
                        );
                        return false;
                    }
                });
    }

    private String formatPaymentType(PaymentType paymentType) {
        return switch (paymentType) {
            case ANNUITY -> "аннуитетный";
            case DIFFERENTIAL -> "дифференцированный";
        };
    }

    private void sendSchedule(
            long chatId,
            CreditSchedule schedule
    ) {
        String scheduleText =
                formatSchedule(schedule);

        for (String part : splitMessage(scheduleText)) {
            sendMessage(chatId, part);
        }
    }

    private String formatSchedule(
            CreditSchedule schedule
    ) {
        StringBuilder result =
                new StringBuilder();

        result.append("График платежей\n\n")
                .append("Переплата: ")
                .append(schedule.totalInterest())
                .append(" руб.\n")
                .append("Общая сумма выплат: ")
                .append(schedule.totalPayment())
                .append(" руб.\n\n");

        for (Payment payment : schedule.payments()) {
            result.append("Месяц ")
                    .append(payment.month())
                    .append(": ")
                    .append(payment.totalPayment())
                    .append(" руб. ")
                    .append("(тело: ")
                    .append(payment.principal())
                    .append(", проценты: ")
                    .append(payment.interest())
                    .append(", остаток: ")
                    .append(payment.remainingDebt())
                    .append(")\n");
        }

        return result.toString();
    }

    private List<String> splitMessage(String text) {
        List<String> parts =
                new ArrayList<>();

        StringBuilder currentPart =
                new StringBuilder();

        for (String line : text.split("\n")) {
            if (currentPart.length() +
                    line.length() + 1 >
                    TELEGRAM_MESSAGE_LIMIT) {

                parts.add(currentPart.toString());
                currentPart.setLength(0);
            }

            currentPart
                    .append(line)
                    .append("\n");
        }

        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }

        return parts;
    }

    private void sendMessage(
            long chatId,
            String text
    ) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(text)
                            .build()
            );
        } catch (TelegramApiException exception) {
            log.error(
                    "Ошибка отправки сообщения в чат {}",
                    chatId,
                    exception
            );

            throw new IllegalStateException(
                    "Не удалось отправить сообщение в Telegram",
                    exception
            );
        }
    }
}