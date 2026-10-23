package com.example.creditbot.bot;

import com.example.creditbot.bot.handler.CommandHandler;
import com.example.creditbot.bot.handler.ConversationHandler;
import com.example.creditbot.bot.support.TelegramMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

/**
 * Принимает обновления Telegram и передаёт их обработчикам.
 */
@Component
public class CreditTelegramBot
        implements SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(CreditTelegramBot.class);

    private final String botToken;
    private final ConversationService conversationService;
    private final CommandHandler commandHandler;
    private final ConversationHandler conversationHandler;
    private final TelegramMessageSender sender;

    public CreditTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            ConversationService conversationService,
            CommandHandler commandHandler,
            ConversationHandler conversationHandler,
            TelegramMessageSender sender
    ) {
        this.botToken = botToken;
        this.conversationService = conversationService;
        this.commandHandler = commandHandler;
        this.conversationHandler = conversationHandler;
        this.sender = sender;
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
        IncomingMessage incoming;

        try {
            incoming = extractIncomingMessage(update);
        } catch (RuntimeException exception) {
            log.error("Не удалось разобрать обновление Telegram", exception);
            return;
        }

        if (incoming == null) {
            return;
        }

        try {
            if (incoming.callbackQueryId() != null) {
                sender.answerCallback(incoming.callbackQueryId());
            }

            UserSession session =
                    conversationService.getSession(incoming.userId());

            if (incoming.text().startsWith("/")) {
                commandHandler.handle(
                        incoming.chatId(),
                        incoming.userId(),
                        extractCommand(incoming.text()),
                        session
                );
                return;
            }

            conversationHandler.handle(
                    incoming.chatId(),
                    incoming.userId(),
                    incoming.text(),
                    session,
                    incoming.callbackQueryId() != null
            );

        } catch (IllegalArgumentException exception) {
            // Не логируем исходный ввод:
            // на одном из шагов это может быть пароль.
            log.warn(
                    "Ошибка ввода пользователя {}: {}",
                    incoming.userId(),
                    exception.getMessage()
            );

            sender.sendSafely(
                    incoming.chatId(),
                    "❌ Ошибка ввода: " + exception.getMessage()
            );

        } catch (Exception exception) {
            log.error(
                    "Ошибка обработки сообщения пользователя {}",
                    incoming.userId(),
                    exception
            );

            sender.sendSafely(
                    incoming.chatId(),
                    """
                            ⚠️ Не удалось завершить действие.
                            Попробуйте /history: расчёт мог быть сохранён.
                            Для возврата в меню используйте /start.
                            """
            );
        }
    }

    private IncomingMessage extractIncomingMessage(Update update) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();

            // Inline-mode callback может не содержать message.
            if (callback.getMessage() == null
                    || callback.getFrom() == null
                    || callback.getData() == null) {
                return null;
            }

            return new IncomingMessage(
                    callback.getMessage().getChatId(),
                    callback.getFrom().getId(),
                    callback.getData(),
                    callback.getId()
            );
        }

        if (update.hasMessage()
                && update.getMessage().hasText()
                && update.getMessage().getFrom() != null) {
            return new IncomingMessage(
                    update.getMessage().getChatId(),
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText().trim(),
                    null
            );
        }

        return null;
    }

    private String extractCommand(String text) {
        String command = text.trim()
                .split("\\s+", 2)[0]
                .toLowerCase(Locale.ROOT);

        int separatorIndex = command.indexOf('@');

        if (separatorIndex > 0) {
            return command.substring(0, separatorIndex);
        }

        return command;
    }

    private record IncomingMessage(
            long chatId,
            long userId,
            String text,
            String callbackQueryId
    ) {
    }
}