package com.example.creditbot.handler;

import com.example.creditbot.bot.ConversationService;
import com.example.creditbot.bot.ConversationStep;
import com.example.creditbot.bot.UserSession;
import com.example.creditbot.bot.keyboard.BotKeyboardFactory;
import com.example.creditbot.bot.message.BotMessageFormatter;
import com.example.creditbot.bot.support.InputParser;
import com.example.creditbot.bot.support.TelegramMessageSender;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Обрабатывает шаги диалогов пользователя.
 */
@Component
@RequiredArgsConstructor
public class ConversationHandler {

    private final ConversationService conversationService;
    private final CreditService creditService;
    private final ManagerAuthService managerAuthService;
    private final InputParser inputParser;
    private final BotKeyboardFactory keyboardFactory;
    private final BotMessageFormatter messageFormatter;
    private final TelegramMessageSender messageSender;

    public void handle(
            long chatId,
            long userId,
            String input,
            UserSession session
    ) {
        switch (session.getStep()) {
            case WAITING_AMOUNT -> handleAmount(
                    chatId,
                    input,
                    session
            );

            case WAITING_TERM -> handleTerm(
                    chatId,
                    input,
                    session
            );

            case WAITING_RATE -> handleRate(
                    chatId,
                    input,
                    session
            );

            case WAITING_PAYMENT_TYPE -> handlePaymentType(
                    chatId,
                    userId,
                    input,
                    session
            );

            case WAITING_MANAGER_PASSWORD -> handleManagerPassword(
                    chatId,
                    userId,
                    input,
                    session
            );

            case NONE -> messageSender.send(
                    chatId,
                    "⚠️ Сначала отправьте /calculate."
            );
        }
    }

    private void handleAmount(
            long chatId,
            String input,
            UserSession session
    ) {
        BigDecimal amount = inputParser.parseAmount(input);

        session.setAmount(amount);
        session.setStep(ConversationStep.WAITING_TERM);

        messageSender.send(
                chatId,
                "📅 Введите срок кредита в месяцах:",
                keyboardFactory.cancelKeyboard()
        );
    }

    private void handleTerm(
            long chatId,
            String input,
            UserSession session
    ) {
        int termMonths = inputParser.parseTermMonths(input);

        session.setTermMonths(termMonths);
        session.setStep(ConversationStep.WAITING_RATE);

        messageSender.send(
                chatId,
                "📈 Введите годовую процентную ставку:",
                keyboardFactory.cancelKeyboard()
        );
    }

    private void handleRate(
            long chatId,
            String input,
            UserSession session
    ) {
        BigDecimal annualRate = inputParser.parseAnnualRate(input);

        session.setAnnualRate(annualRate);
        session.setStep(
                ConversationStep.WAITING_PAYMENT_TYPE
        );

        messageSender.send(
                chatId,
                "⚖️ Выберите тип платежа:",
                keyboardFactory.paymentTypeKeyboard()
        );
    }

    private void handlePaymentType(
            long chatId,
            long userId,
            String input,
            UserSession session
    ) {
        PaymentType paymentType =
                inputParser.parsePaymentType(input);

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

        messageSender.send(
                chatId,
                messageFormatter.formatSchedule(schedule)
        );

        conversationService.clear(userId);

        messageSender.send(
                chatId,
                "✅ Расчёт сохранён. Выберите дальнейшее действие:",
                keyboardFactory.startKeyboard()
        );
    }

    private void handleManagerPassword(
            long chatId,
            long userId,
            String input,
            UserSession session
    ) {
        boolean authenticated =
                managerAuthService.authenticate(
                        userId,
                        input
                );

        session.reset();
        conversationService.clear(userId);

        if (authenticated) {
            messageSender.send(
                    chatId,
                    """
                    ✅ Авторизация успешно выполнена.

                    Теперь доступна команда:
                    /analytics
                    """
            );
            return;
        }

        messageSender.send(chatId, "❌ Неверный пароль.");
    }
}