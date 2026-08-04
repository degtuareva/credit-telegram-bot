package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final CreditRequestRepository repository;

    public HistoryService(CreditRequestRepository repository) {
        this.repository = repository;
    }

    public List<CreditRequest> getUserHistory(long userId) {
        return repository.findByTelegramUserId(userId);
    }
}