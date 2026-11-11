package com.example.creditbot.web.security;

import com.example.creditbot.security.ManagerAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UnifiedAuthService {

    private final TelegramInitDataService telegramAuthService;
    private final JwtService jwtService;
    private final ManagerAuthService managerAuthService;

    public UnifiedAuthService(
            TelegramInitDataService telegramAuthService,
            JwtService jwtService,
            ManagerAuthService managerAuthService
    ) {
        this.telegramAuthService = telegramAuthService;
        this.jwtService = jwtService;
        this.managerAuthService = managerAuthService;
    }

    public Long resolveUserId(
            String initData,
            String authorization
    ) {
        if (initData != null && !initData.isBlank()) {
            TelegramUser telegramUser =
                    telegramAuthService.authenticate(initData);

            return telegramUser.id();
        }

        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            String token = authorization.substring(7);

            return jwtService.extractUserId(token);
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Требуется авторизация."
        );
    }

    public boolean isManager(Long userId) {
        return managerAuthService.isAllowedManager(userId);
    }
}