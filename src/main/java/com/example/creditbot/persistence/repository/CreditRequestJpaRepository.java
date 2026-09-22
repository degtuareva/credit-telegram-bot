package com.example.creditbot.persistence.repository;

import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.persistence.entity.CreditRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface CreditRequestJpaRepository
        extends JpaRepository<CreditRequestEntity, Long> {

    List<CreditRequestEntity> findByTelegramUserIdOrderByCreatedAtDesc(
            Long telegramUserId
    );

    List<CreditRequestEntity> findByAmountBetweenAndPaymentType(
            BigDecimal minAmount,
            BigDecimal maxAmount,
            PaymentType paymentType
    );
}