package com.example.creditbot.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * Создаёт клавиатуры Telegram-бота.
 */
@Component
@SuppressWarnings("deprecation")
public class BotKeyboardFactory {

    public InlineKeyboardMarkup startKeyboard() {
        InlineKeyboardRow calculationRow = new InlineKeyboardRow(
                button("📊 Рассчитать кредит", "/calculate")
        );

        InlineKeyboardRow navigationRow = new InlineKeyboardRow(
                button("📜 История", "/history"),
                button("❓ Помощь", "/help")
        );

        return new InlineKeyboardMarkup(
                List.of(calculationRow, navigationRow)
        );
    }

    public InlineKeyboardMarkup helpKeyboard() {
        InlineKeyboardRow calculationRow = new InlineKeyboardRow(
                button("📊 Рассчитать кредит", "/calculate")
        );

        InlineKeyboardRow historyRow = new InlineKeyboardRow(
                button("📜 История", "/history")
        );

        return new InlineKeyboardMarkup(
                List.of(calculationRow, historyRow)
        );
    }

    public InlineKeyboardMarkup newCalculationKeyboard() {
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
        InlineKeyboardRow typesRow = new InlineKeyboardRow(
                button("Аннуитетный", "1"),
                button("Дифференцированный", "2")
        );

        InlineKeyboardRow cancelRow = new InlineKeyboardRow(
                button("❌ Отмена", "/cancel")
        );

        return new InlineKeyboardMarkup(
                List.of(typesRow, cancelRow)
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