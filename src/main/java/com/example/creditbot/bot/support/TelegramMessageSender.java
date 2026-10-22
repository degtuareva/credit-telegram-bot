package com.example.creditbot.support;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Отправляет сообщения пользователям Telegram.
 */
@Component
@RequiredArgsConstructor
public class TelegramMessageSender {

    private static final Logger log =
            LoggerFactory.getLogger(TelegramMessageSender.class);

    private final TelegramClient telegramClient;

    public void send(long chatId, String text) {
        send(chatId, text, null);
    }

    public void send(
            long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException exception) {
            log.error(
                    "Не удалось отправить сообщение в чат {}",
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