package com.example.creditbot.web;

import com.example.creditbot.security.ManagerAuthService;
import com.example.creditbot.service.ManagerAnalyticsService;
import com.example.creditbot.web.dto.ManagerAnalyticsResponse;
import com.example.creditbot.web.security.TelegramInitDataService;
import com.example.creditbot.web.security.TelegramUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mini-app/manager")
public class ManagerAnalyticsController {

    private final TelegramInitDataService telegramAuth;
    private final ManagerAuthService managerAuth;
    private final ManagerAnalyticsService analyticsService;

    public ManagerAnalyticsController(TelegramInitDataService telegramAuth, ManagerAuthService managerAuth, ManagerAnalyticsService analyticsService) {
        this.telegramAuth = telegramAuth;
        this.managerAuth = managerAuth;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<ManagerAnalyticsResponse> analytics(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,


            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        TelegramUser user = telegramAuth.authenticate(initData);

        if (!managerAuth.isAllowedManager(user.id())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Доступ разрешен только менеджерам."
            );
        }

        if (!managerAuth.isAuthenticated(user.id())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Сначала войдите как менеджер в чате бота,затем обновите аналитику."
            );
        }
        if (to.isBefore(from)
                || java.time.temporal.ChronoUnit.DAYS
                .between(from, to) >= 366) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Выберите период от 1 до 366 дней."
            );
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(analyticsService.summarize(from, to));
    }
}
