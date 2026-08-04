package com.example.creditbot.repository;

import com.example.creditbot.domain.CreditRequest;

import java.util.List;
import java.util.Optional;

public interface CreditRequestRepository {

    CreditRequest save(CreditRequest request);

    List<CreditRequest> findByTelegramUserId(long telegramUserId);

    List<CreditRequest> findAll();

    Optional<CreditRequest> findById(long id);
}