package com.example.creditbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = {
        "com.example.creditbot.persistence.repository",
        "com.example.creditbot.repository"
})
@EntityScan(basePackages = {
        "com.example.creditbot.persistence.entity",
        "com.example.creditbot.domain"
})
public class CreditBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBotApplication.class, args);
    }
}