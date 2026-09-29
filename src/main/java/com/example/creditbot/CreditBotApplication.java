package com.example.creditbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Главный класс приложения Spring Boot.
 * <p>
 * Запускает контекст Spring и инициализирует все компоненты приложения,
 * включая Telegram-бота, сервисы и репозитории.
 * </p>
 *
 * @author degtuareva
 * @version 1.0
 * @see SpringBootApplication
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.creditbot.persistence.repository")
@EntityScan(basePackages = "com.example.creditbot.persistence.entity")
public class CreditBotApplication {
    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */

    public static void main(String[] args) {
        SpringApplication.run(CreditBotApplication.class, args);
    }
}