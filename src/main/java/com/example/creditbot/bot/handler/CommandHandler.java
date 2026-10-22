package com.example.creditbot.handler;

import com.example.creditbot.bot.ConversationService;
import com.example.creditbot.bot.ConversationStep;
import com.example.creditbot.bot.UserSession;
import com.example.creditbot.bot.keyboard.BotKeyboardFactory;
import com.example.creditbot.bot.message.BotMessageFormatter;
import com.example.creditbot.bot.support.TelegramMessageSender;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.AnalyticsService;
import com.example.creditbot.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Обрабатывает команды Telegram-бота.
 */
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final ConversationService conversationService;
    private final HistoryService historyService;
    private final AnalyticsService analyticsService;
    private final ManagerAuthService managerAuthService;
    private final BotKeyboardFactory keyboardFactory;
    private final BotMessageFormatter messageFormatter;
    private final TelegramMessageSender messageSender;

    public void handle(
            long chatId,
            long userId,
            String command,
            UserSession session
    ) {
        switch (command) {
            case "/start" -> start(chatId, userId);

            case "/help" -> help(chatId);

            case "/calculate" -> calculate(
                    chatId,
                    userId,
                    session
            );

            case "/history" -> history(chatId, userId);

            case "/manager_login" -> managerLogin(
                    chatId,
                    userId,
                    session
            );

            case "/manager_logout" -> managerLogout(
                    chatId,
                    userId
            );

            case "/analytics" -> analytics(chatId, userId);

            case "/cancel" -> cancel(chatId, userId);

            default -> messageSender.send(
                    chatId,
                    "❓ Неизвестная команда. Используйте /help."
            );
        }
    }

    private void start(long chatId, long userId) {
        conversationService.clear(userId);

        messageSender.send(
                chatId,
                messageFormatter.startMessage(),
                keyboardFactory.startKeyboard()
        );
    }

    private void help(long chatId) {
        messageSender.send(
                chatId,
                messageFormatter.helpMessage(),
                keyboardFactory.helpKeyboard()
        );
    }

    private void calculate(
            long chatId,
            long userId,
            UserSession session
    ) {
        session.reset();
        session.setStep(ConversationStep.WAITING_AMOUNT);

        messageSender.send(
                chatId,
                "💰 Введите сумму кредита:",
                keyboardFactory.cancelKeyboard()
        );
    }

    private void history(long chatId, long userId) {
        String text = messageFormatter.formatHistory(
                historyService.getUserHistory(userId)
        );

        messageSender.send(
                chatId,
                text,
                keyboardFactory.calculationFinishedKeyboard()
        );
    }

    private void managerLogin(
            long chatId,
            long userId,
            UserSession session
    ) {
        if (!managerAuthService.isAllowedManager(userId)) {
            messageSender.send(
                    chatId,
                    "⛔ Доступ запрещён. Вы не являетесь менеджером."
            );
            return;
        }

        session.reset();
        session.setStep(
                ConversationStep.WAITING_MANAGER_PASSWORD
        );

        messageSender.send(
                chatId,
                "🔑 Введите пароль менеджера:",
                keyboardFactory.cancelKeyboard()
        );
    }

    private void managerLogout(
            long chatId,
            long userId
    ) {
        managerAuthService.logout(userId);

        messageSender.send(
                chatId,
                "🔒 Вы вышли из менеджерской сессии."
        );
    }

    private void analytics(long chatId, long userId) {
        if (!managerAuthService.isAuthenticated(userId)) {
            messageSender.send(
                    chatId,
                    """
                    ⛔ Доступ запрещён.

                    Для входа используйте команду:
                    /manager_login
                    """
            );
            return;
        }

        long totalRequests =
                analyticsService.totalRequests();

        Map<PaymentType, Long> paymentTypes =
                analyticsService.paymentTypeStats();

        Map<Integer, Long> terms =
                analyticsService.termStats();

        messageSender.send(
                chatId,
                messageFormatter.formatAnalytics(
                        totalRequests,
                        paymentTypes,
                        terms
                )
        );
    }

    private void cancel(long chatId, long userId) {
        conversationService.clear(userId);

        messageSender.send(
                chatId,
                "Действие отменено.",
                keyboardFactory.startKeyboard()
        );
    }
}