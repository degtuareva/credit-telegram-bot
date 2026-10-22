package com.example.creditbot.bot.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Отправляет сообщения и подтверждает нажатия кнопок.
 */
@Component
public class TelegramMessageSender {

    private static final Logger log =
            LoggerFactory.getLogger(TelegramMessageSender.class);

    private static final int MESSAGE_LIMIT = 4000;

    private final TelegramClient telegramClient;

    public TelegramMessageSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void send(long chatId, String text) {
        send(chatId, text, null);
    }

    public void send(
            long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        List<String> parts = splitMessage(text);

        for (int index = 0; index < parts.size(); index++) {
            SendMessage.SendMessageBuilder builder =
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(parts.get(index));

            if (keyboard != null && index == parts.size() - 1) {
                builder.replyMarkup(keyboard);
            }

            try {
                telegramClient.execute(builder.build());
            } catch (TelegramApiException exception) {
                log.error(
                        "Не удалось отправить сообщение в чат {}",
                        chatId,
                        exception
                );

                throw new IllegalStateException(
                        "Не удалось отправить сообщение в Telegram.",
                        exception
                );
            }
        }
    }

    /**
     * Используется в обработчиках ошибок:
     * повторная ошибка отправки не выходит наружу.
     */
    public void sendSafely(long chatId, String text) {
        try {
            send(chatId, text);
        } catch (RuntimeException exception) {
            log.warn(
                    "Не удалось отправить уведомление об ошибке в чат {}",
                    chatId,
                    exception
            );
        }
    }

    public void answerCallback(String callbackQueryId) {
        try {
            telegramClient.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQueryId)
                            .build()
            );
        } catch (TelegramApiException exception) {
            log.warn(
                    "Не удалось подтвердить нажатие кнопки {}",
                    callbackQueryId,
                    exception
            );
        }
    }

    /**
     * Делит текст по границам строк, а слишком длинную
     * строку — на отдельные фрагменты.
     */
    public List<String> splitMessage(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Текст сообщения не должен быть пустым."
            );
        }

        List<String> parts = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + MESSAGE_LIMIT, text.length());

            if (end < text.length()) {
                int newline = text.lastIndexOf('\n', end - 1);

                if (newline >= start) {
                    end = newline + 1;
                }

                // Не разрываем UTF-16 пару, например emoji.
                if (end > start
                        && Character.isHighSurrogate(text.charAt(end - 1))
                        && Character.isLowSurrogate(text.charAt(end))) {
                    end--;
                }
            }

            String part = text.substring(start, end);

            if (!part.isBlank()) {
                parts.add(part);
            }

            start = end;
        }

        return parts;
    }
}