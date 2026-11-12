package com.example.creditbot.domain;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Setter
    @Column(nullable = false)
    private String firstName;

    @Setter
    @Column
    private String lastName;

    @Setter
    @Column(nullable = false)
    private boolean manager = false;

    @Setter
    @Column(name = "telegram_user_id")
    private Long telegramUserId;

    @Setter
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isManager() {
        return manager;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}