package com.example.creditbot.bot.handler;

import com.example.creditbot.bot.ConversationService;
import com.example.creditbot.bot.ConversationStep;
import com.example.creditbot.bot.UserSession;
import com.example.creditbot.bot.keyboard.BotKeyboardFactory;
import com.example.creditbot.bot.message.BotMessageFormatter;
import com.example.creditbot.bot.support.InputParser;
import com.example.creditbot.bot.support.TelegramMessageSender;
import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.CreditService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Обрабатывает ввод на шагах диалога.
 */
@Component
public class ConversationHandler {

    private final ConversationService conversationService;
    private final CreditService creditService;
    private final ManagerAuthService managerAuthService;
    private final InputParser parser;
    private final BotKeyboardFactory keyboards;
    private final BotMessageFormatter formatter;
    private final TelegramMessageSender sender;

    public ConversationHandler(
            ConversationService conversationService,
            CreditService creditService,
            ManagerAuthService managerAuthService,
            InputParser parser,
            BotKeyboardFactory keyboards,
            BotMessageFormatter formatter,
            TelegramMessageSender sender
    ) {
        this.conversationService = conversationService;
        this.creditService = creditService;
        this.managerAuthService = managerAuthService;
        this.parser = parser;
        this.keyboards = keyboards;
        this.formatter = formatter;
        this.sender = sender;
    }

    public void handle(
            long chatId,
            long userId,
            String input,
            UserSession session,
            boolean fromCallback
    ) {
        // Старые кнопки выбора платежа не должны становиться
        // суммой, сроком или паролем в новом диалоге.
        if (fromCallback
                && session.getStep()
                != ConversationStep.WAITING_PAYMENT_TYPE) {
            sender.send(
                    chatId,
                    "Эта кнопка сейчас неактуальна. "
                            + "Продолжите текущий ввод или отправьте /cancel."
            );
            return;
        }

        switch (session.getStep()) {
            case WAITING_AMOUNT ->
                    handleAmount(chatId, input, session);

            case WAITING_TERM ->
                    handleTerm(chatId, input, session);

            case WAITING_RATE ->
                    handleRate(chatId, input, session);

            case WAITING_PAYMENT_TYPE ->
                    handlePaymentType(
                            chatId,
                            userId,
                            input,
                            session
                    );

            case WAITING_MANAGER_PASSWORD ->
                    handleManagerPassword(chatId, userId, input);

            case NONE -> sender.send(
                    chatId,
                    "❓ Используйте /calculate или /help."
            );
        }
    }

    private void handleAmount(
            long chatId,
            String input,
            UserSession session
    ) {
        session.setAmount(parser.parseAmount(input));
        session.setStep(ConversationStep.WAITING_TERM);

        sender.send(
                chatId,
                "📅 Введите срок кредита в месяцах:",
                keyboards.cancelKeyboard()
        );
    }

    private void handleTerm(
            long chatId,
            String input,
            UserSession session
    ) {
        session.setTermMonths(parser.parseTermMonths(input));
        session.setStep(ConversationStep.WAITING_RATE);

        sender.send(
                chatId,
                "📈 Введите годовую процентную ставку:",
                keyboards.cancelKeyboard()
        );
    }

    private void handleRate(
            long chatId,
            String input,
            UserSession session
    ) {
        session.setAnnualRate(parser.parseAnnualRate(input));
        session.setStep(ConversationStep.WAITING_PAYMENT_TYPE);

        sender.send(
                chatId,
                "⚖️ Выберите тип платежа:",
                keyboards.paymentTypeKeyboard()
        );
    }

    private void handlePaymentType(
            long chatId,
            long userId,
            String input,
            UserSession session
    ) {
        session.setPaymentType(parser.parsePaymentType(input));

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

        // Расчёт уже сохранён. Завершаем диалог до отправки,
        // чтобы ошибка сети не оставляла его на шаге сохранения.
        conversationService.clear(userId);

        sender.send(
                chatId,
                formatter.formatSchedule(schedule)
        );

        sender.send(
                chatId,
                "✅ Расчёт сохранён. Выберите дальнейшее действие:",
                keyboards.startKeyboard()
        );
    }

    private void handleManagerPassword(
            long chatId,
            long userId,
            String input
    ) {
        boolean authenticated =
                managerAuthService.authenticate(userId, input);

        conversationService.clear(userId);

        if (authenticated) {
            sender.send(
                    chatId,
                    """
                    ✅ Авторизация успешно выполнена.

                    Теперь доступна команда:
                    /analytics
                    """
            );
        } else {
            sender.send(
                    chatId,
                    "❌ Неверный пароль. Для повторного входа "
                            + "отправьте /manager_login."
            );
        }
    }
}