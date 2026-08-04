package com.example.creditbot.bot;

import org.springframework.stereotype.Component;

@Component
public class BotCommandHandler {

    public String handleCommand(String text) {
        return switch (text) {
            case "/start" -> """
                    Добро пожаловать в кредитный калькулятор!

                    Доступные команды:
                    /calculate — рассчитать кредит
                    /history — история расчётов
                    /help — помощь
                    """;

            case "/calculate" -> "Введите сумму кредита, например: 1000000";

            case "/history" -> "История расчётов пока находится в разработке.";

            case "/help" -> """
                    Как рассчитать кредит:

                    1. Введите /calculate.
                    2. Укажите сумму кредита.
                    3. Укажите срок в месяцах.
                    4. Укажите годовую ставку.
                    5. Выберите тип платежа.
                    """;

            default -> null;
        };
    }
}