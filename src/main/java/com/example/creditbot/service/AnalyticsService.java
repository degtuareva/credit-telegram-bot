package com.example.creditbot.service;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.PaymentType;
import com.example.creditbot.repository.CreditRequestRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final CreditRequestRepository repository;

    public AnalyticsService(CreditRequestRepository repository) {
        this.repository = repository;
    }

    public long totalRequests() {
        return repository.findAll().size();
    }

    public Map<PaymentType, Long> paymentTypeStats() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        CreditRequest::paymentType,
                        Collectors.counting()
                ));
    }

    public Map<Integer, Long> termStats() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        CreditRequest::termMonths,
                        Collectors.counting()
                ));
    }

    public Map<BigDecimal, Long> amountStats() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        CreditRequest::amount,
                        Collectors.counting()
                ));
    }

    public List<CreditRequest> filter(
            BigDecimal minAmount,
            BigDecimal maxAmount,
            PaymentType paymentType
    ) {
        return repository.findAll().stream()
                .filter(request -> minAmount == null || request.amount().compareTo(minAmount) >= 0)
                .filter(request -> maxAmount == null || request.amount().compareTo(maxAmount) <= 0)
                .filter(request -> paymentType == null || request.paymentType() == paymentType)
                .toList();
    }
}