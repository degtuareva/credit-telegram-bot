package com.example.creditbot.web.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Проверяет подпись Telegram Mini App initData.
 */
@Service
public class TelegramInitDataService {

    private final String botToken;
    private final ObjectMapper objectMapper;
    private final long maxAgeSeconds;

    public TelegramInitDataService(
            @Value("${telegram.bot.token}") String botToken,
            ObjectMapper objectMapper,
            @Value("${mini-app.auth.max-age-seconds:3600}")
            long maxAgeSeconds
    ) {
        this.botToken = botToken;
        this.objectMapper = objectMapper;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public TelegramUser authenticate(String rawInitData) {
        if (rawInitData == null
                || rawInitData.isBlank()
                || rawInitData.length() > 16384) {
            throw unauthorized();
        }

        try {
            Map<String, String> parameters = parse(rawInitData);

            String receivedHash = parameters.remove("hash");
            if (receivedHash == null || receivedHash.length() != 64) {
                throw unauthorized();
            }

            String dataCheckString = parameters.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));

            byte[] secretKey = hmacSha256(
                    "WebAppData".getBytes(StandardCharsets.UTF_8),
                    botToken
            );

            byte[] calculatedHash = hmacSha256(
                    secretKey,
                    dataCheckString
            );

            byte[] expectedHash = HexFormat.of().parseHex(receivedHash);

            if (!MessageDigest.isEqual(calculatedHash, expectedHash)) {
                throw unauthorized();
            }

            long authDate = Long.parseLong(
                    parameters.getOrDefault("auth_date", "")
            );

            long now = Instant.now().getEpochSecond();

            if (authDate > now + 30
                    || now - authDate > maxAgeSeconds) {
                throw unauthorized();
            }

            String userJson = parameters.get("user");
            if (userJson == null) {
                throw unauthorized();
            }

            JsonNode user = objectMapper.readTree(userJson);
            JsonNode idNode = user.get("id");

            if (idNode == null
                    || !idNode.isIntegralNumber()
                    || !idNode.canConvertToLong()
                    || idNode.asLong() <= 0) {
                throw unauthorized();
            }

            return new TelegramUser(
                    idNode.asLong(),
                    user.path("first_name").asText(""),
                    user.path("username").asText("")
            );

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            // Не записываем initData или токен в лог.
            throw unauthorized();
        }
    }

    private Map<String, String> parse(String rawInitData) {
        Map<String, String> parameters = new TreeMap<>();

        for (String pair : rawInitData.split("&")) {
            int separator = pair.indexOf('=');

            if (separator <= 0) {
                throw unauthorized();
            }

            String key = URLDecoder.decode(
                    pair.substring(0, separator),
                    StandardCharsets.UTF_8
            );

            String value = URLDecoder.decode(
                    pair.substring(separator + 1),
                    StandardCharsets.UTF_8
            );

            if (parameters.putIfAbsent(key, value) != null) {
                throw unauthorized();
            }
        }

        return parameters;
    }

    private byte[] hmacSha256(byte[] key, String value)
            throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));

        return mac.doFinal(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Не удалось подтвердить вход. Закройте приложение "
                        + "и откройте его снова из Telegram."
        );
    }
}