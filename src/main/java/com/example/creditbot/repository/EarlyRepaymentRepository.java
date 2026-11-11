package com.example.creditbot.repository;

import com.example.creditbot.domain.EarlyRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EarlyRepaymentRepository
        extends JpaRepository<EarlyRepayment, Long> {

    List<EarlyRepayment> findByTelegramUserIdOrderByCreatedAtDesc(
            Long telegramUserId
    );
}