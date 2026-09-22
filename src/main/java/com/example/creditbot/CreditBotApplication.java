package com.example.creditbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Spring Boot.
 * <p>
 * Запускает контекст Spring и инициализирует все компоненты приложения,
 * включая Telegram-бота, сервисы и репозитории.
 * </p>
 *
 * @author Ваше Имя
 * @version 1.0
 * @see SpringBootApplication
 */
@SpringBootApplication
public class CreditBotApplication {
    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */

    public static void main(String[] args) {
        SpringApplication.run(
                CreditBotApplication.class,
                args
        );
    }
}