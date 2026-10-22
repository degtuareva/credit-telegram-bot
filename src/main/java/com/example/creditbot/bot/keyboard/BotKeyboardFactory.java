package com.example.creditbot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * Создаёт инлайн-клавиатуры Telegram-бота.
 */
@Component
@SuppressWarnings("deprecation")
public class BotKeyboardFactory {

    public InlineKeyboardMarkup startKeyboard() {
        InlineKeyboardRow firstRow = new InlineKeyboardRow(
                button("📊 Рассчитать кредит", "/calculate")
        );

        InlineKeyboardRow secondRow = new InlineKeyboardRow(
                button("📜 История", "/history"),
                button("❓ Помощь", "/help")
        );

        return new InlineKeyboardMarkup(
                List.of(firstRow, secondRow)
        );
    }

    public InlineKeyboardMarkup helpKeyboard() {
        InlineKeyboardRow firstRow = new InlineKeyboardRow(
                button("📊 Рассчитать кредит", "/calculate")
        );

        InlineKeyboardRow secondRow = new InlineKeyboardRow(
                button("📜 История", "/history")
        );

        return new InlineKeyboardMarkup(
                List.of(firstRow, secondRow)
        );
    }

    public InlineKeyboardMarkup calculationFinishedKeyboard() {
        InlineKeyboardRow row = new InlineKeyboardRow(
                button("🔄 Новый расчёт", "/calculate")
        );

        return new InlineKeyboardMarkup(List.of(row));
    }

    public InlineKeyboardMarkup cancelKeyboard() {
        InlineKeyboardRow row = new InlineKeyboardRow(
                button("❌ Отмена", "/cancel")
        );

        return new InlineKeyboardMarkup(List.of(row));
    }

    public InlineKeyboardMarkup paymentTypeKeyboard() {
        InlineKeyboardRow paymentTypes = new InlineKeyboardRow(
                button("Аннуитетный", "1"),
                button("Дифференцированный", "2")
        );

        InlineKeyboardRow cancelRow = new InlineKeyboardRow(
                button("❌ Отмена", "/cancel")
        );

        return new InlineKeyboardMarkup(
                List.of(paymentTypes, cancelRow)
        );
    }

    private InlineKeyboardButton button(
            String text,
            String callbackData
    ) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}