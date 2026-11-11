package com.example.creditbot.web;

import com.example.creditbot.domain.CreditRequest;
import com.example.creditbot.domain.CreditSchedule;
import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.CalculationComparisonService;
import com.example.creditbot.service.CreditService;
import com.example.creditbot.service.EarlyRepaymentService;
import com.example.creditbot.service.HistoryService;
import com.example.creditbot.web.dto.*;
import com.example.creditbot.web.security.TelegramUser;
import com.example.creditbot.web.security.UnifiedAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/mini-app")
public class MiniAppController {

    private final CreditService creditService;
    private final HistoryService historyService;
    private final UnifiedAuthService authService;
    private final ManagerAuthService managerAuthService;
    private final CalculationComparisonService comparisonService;
    private final EarlyRepaymentService earlyRepaymentService;

    public MiniAppController(
            CreditService creditService,
            HistoryService historyService,
            UnifiedAuthService authService,
            ManagerAuthService managerAuthService,
            CalculationComparisonService comparisonService,
            EarlyRepaymentService earlyRepaymentService
    ) {
        this.creditService = creditService;
        this.historyService = historyService;
        this.authService = authService;
        this.managerAuthService = managerAuthService;
        this.comparisonService = comparisonService;
        this.earlyRepaymentService = earlyRepaymentService;
    }

    @GetMapping("/me")
    public MiniAppResponse me(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        boolean manager = managerAuthService.isAllowedManager(userId);

        return new MiniAppResponse(
                userId,
                null,
                null,
                manager,
                manager && managerAuthService.isAuthenticated(userId)
        );
    }

    @PostMapping("/calculations")
    public CreditSchedule calculate(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @Valid @RequestBody CalculationRequest input
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        CreditRequest request = new CreditRequest(
                0L,
                userId,
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
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        return historyService.getUserHistory(userId)
                .stream()
                .sorted(
                        Comparator.comparing(CreditRequest::createdAt)
                                .reversed()
                )
                .limit(50)
                .toList();
    }

    @PostMapping("/calculations/compare")
    public CalculationComparisonResponse compare(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @Valid @RequestBody CalculationComparisonRequest request
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        return comparisonService.compare(
                userId,
                request
        );
    }

    @DeleteMapping("/calculation/{id}")
    public ResponseEntity<Void> deleteCalculation(
            @PathVariable Long id,
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        historyService.deleteUserCalculation(userId, id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calculations/early-repayment")
    public EarlyRepaymentResponse earlyRepayment(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @Valid @RequestBody EarlyRepaymentRequest request
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        return earlyRepaymentService.calculate(
                userId,
                request
        );
    }

    @GetMapping("/history/export")
    public ResponseEntity<byte[]> exportHistory(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {
        Long userId = authService.resolveUserId(
                initData,
                authorization
        );

        List<CreditRequest> history =
                historyService.getUserHistory(userId);

        StringBuilder csv = new StringBuilder();
        csv.append("id;amount;term_months;annual_rate;payment_type;created_at\n");

        for (CreditRequest item : history) {
            csv.append(item.id()).append(";")
                    .append(item.amount()).append(";")
                    .append(item.termMonths()).append(";")
                    .append(item.annualRate()).append(";")
                    .append(item.paymentType()).append(";")
                    .append(item.createdAt()).append("\n");
        }

        byte[] content = csv.toString()
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=credit-history.csv"
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }
}