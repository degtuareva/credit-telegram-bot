package com.example.creditbot.web;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.service.CreditService;
import com.example.creditbot.service.HistoryService;
import com.example.creditbot.web.dto.CalculationRequest;
import com.example.creditbot.web.security.TelegramInitDataService;
import com.example.creditbot.web.security.TelegramUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/mini-app")
public class MiniAppController {

    private final CreditService creditService;
    private final HistoryService historyService;
    private final TelegramInitDataService authService;

    public MiniAppController(
            CreditService creditService,
            HistoryService historyService,
            TelegramInitDataService authService
    ) {
        this.creditService = creditService;
        this.historyService = historyService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public TelegramUser me(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData
    ) {
        return authService.authenticate(initData);
    }

    @PostMapping("/calculations")
    public CreditSchedule calculate(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @Valid @RequestBody CalculationRequest input
    ) {
        TelegramUser user = authService.authenticate(initData);

        CreditRequest request = new CreditRequest(
                0L,
                user.id(),
                input.amount(),
                input.termMonths(),
                input.annualRate(),
                input.paymentType(),
                LocalDateTime.now()
        );

        return creditService.calculateAndSave(request);
    }

    @GetMapping("/history")
    public List<CreditRequest> history(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData
    ) {
        TelegramUser user = authService.authenticate(initData);

        return historyService.getUserHistory(user.id())
                .stream()
                .sorted(
                        Comparator.comparing(CreditRequest::createdAt)
                                .reversed()
                )
                .limit(50)
                .toList();
    }
}