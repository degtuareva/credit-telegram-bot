package com.example.creditbot.bot.handler;

import com.example.creditbot.bot.ConversationService;
import com.example.creditbot.bot.ConversationStep;
import com.example.creditbot.bot.UserSession;
import com.example.creditbot.bot.keyboard.BotKeyboardFactory;
import com.example.creditbot.bot.message.BotMessageFormatter;
import com.example.creditbot.bot.support.TelegramMessageSender;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.AnalyticsService;
import com.example.creditbot.service.HistoryService;
import org.springframework.stereotype.Component;

/**
 * Обрабатывает команды бота.
 */
@Component
public class CommandHandler {

    private final ConversationService conversationService;
    private final HistoryService historyService;
    private final AnalyticsService analyticsService;
    private final ManagerAuthService managerAuthService;
    private final BotKeyboardFactory keyboards;
    private final BotMessageFormatter formatter;
    private final TelegramMessageSender sender;

    public CommandHandler(
            ConversationService conversationService,
            HistoryService historyService,
            AnalyticsService analyticsService,
            ManagerAuthService managerAuthService,
            BotKeyboardFactory keyboards,
            BotMessageFormatter formatter,
            TelegramMessageSender sender
    ) {
        this.conversationService = conversationService;
        this.historyService = historyService;
        this.analyticsService = analyticsService;
        this.managerAuthService = managerAuthService;
        this.keyboards = keyboards;
        this.formatter = formatter;
        this.sender = sender;
    }

    public void handle(
            long chatId,
            long userId,
            String command,
            UserSession session
    ) {
        switch (command) {
            case "/start" -> start(chatId, userId);

            case "/help" -> sender.send(
                    chatId,
                    formatter.helpMessage(),
                    keyboards.helpKeyboard()
            );

            case "/calculate" -> calculate(chatId, session);

            case "/history" -> history(chatId, userId);

            case "/manager_login" ->
                    managerLogin(chatId, userId, session);

            case "/manager_logout" ->
                    managerLogout(chatId, userId);

            case "/analytics" -> analytics(chatId, userId);

            case "/cancel" -> cancel(chatId, userId);

            default -> sender.send(
                    chatId,
                    "❓ Неизвестная команда. Используйте /help."
            );
        }
    }

    private void start(long chatId, long userId) {
        conversationService.clear(userId);

        sender.send(
                chatId,
                formatter.startMessage(),
                keyboards.startKeyboard()
        );
    }

    private void calculate(long chatId, UserSession session) {
        session.reset();
        session.setStep(ConversationStep.WAITING_AMOUNT);

        sender.send(
                chatId,
                "💰 Введите сумму кредита:",
                keyboards.cancelKeyboard()
        );
    }

    private void history(long chatId, long userId) {
        sender.send(
                chatId,
                formatter.formatHistory(
                        historyService.getUserHistory(userId)
                ),
                keyboards.newCalculationKeyboard()
        );
    }

    private void managerLogin(
            long chatId,
            long userId,
            UserSession session
    ) {
        if (!managerAuthService.isAllowedManager(userId)) {
            sender.send(
                    chatId,
                    "⛔ Доступ запрещён. Вы не являетесь менеджером."
            );
            return;
        }

        session.reset();
        session.setStep(
                ConversationStep.WAITING_MANAGER_PASSWORD
        );

        sender.send(
                chatId,
                "🔑 Введите пароль менеджера:",
                keyboards.cancelKeyboard()
        );
    }

    private void managerLogout(long chatId, long userId) {
        managerAuthService.logout(userId);
        conversationService.clear(userId);

        sender.send(
                chatId,
                "🔒 Вы вышли из менеджерской сессии.",
                keyboards.startKeyboard()
        );
    }

    private void analytics(long chatId, long userId) {
        if (!managerAuthService.isAuthenticated(userId)) {
            sender.send(
                    chatId,
                    """
                    ⛔ Доступ запрещён.

                    Для входа используйте:
                    /manager_login
                    """
            );
            return;
        }

        sender.send(
                chatId,
                formatter.formatAnalytics(
                        analyticsService.totalRequests(),
                        analyticsService.paymentTypeStats(),
                        analyticsService.termStats()
                )
        );
    }

    private void cancel(long chatId, long userId) {
        conversationService.clear(userId);

        sender.send(
                chatId,
                "Действие отменено. Главное меню:",
                keyboards.startKeyboard()
        );
    }
}