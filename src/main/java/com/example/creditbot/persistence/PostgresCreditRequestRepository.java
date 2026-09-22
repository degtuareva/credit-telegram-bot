package com.example.creditbot.persistence;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.persistence.entity.CreditRequestEntity;
import com.example.creditbot.persistence.repository.CreditRequestJpaRepository;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("postgres")
public class PostgresCreditRequestRepository
        implements CreditRequestRepository {

    private final CreditRequestJpaRepository jpaRepository;

    public PostgresCreditRequestRepository(
            CreditRequestJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CreditRequest save(CreditRequest request) {
        CreditRequestEntity entity =
                new CreditRequestEntity(
                        request.telegramUserId(),
                        request.amount(),
                        request.termMonths(),
                        request.annualRate(),
                        request.paymentType(),
                        request.createdAt()
                );

        CreditRequestEntity saved =
                jpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public List<CreditRequest> findByTelegramUserId(
            long telegramUserId
    ) {
        return jpaRepository
                .findByTelegramUserIdOrderByCreatedAtDesc(
                        telegramUserId
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CreditRequest> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CreditRequest> findById(long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    private CreditRequest toDomain(
            CreditRequestEntity entity
    ) {
        return new CreditRequest(
                entity.getId(),
                entity.getTelegramUserId(),
                entity.getAmount(),
                entity.getTermMonths(),
                entity.getAnnualRate(),
                entity.getPaymentType(),
                entity.getCreatedAt()
        );
    }
}