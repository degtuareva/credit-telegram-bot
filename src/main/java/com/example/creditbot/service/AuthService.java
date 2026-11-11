package com.example.creditbot.service;

import com.example.creditbot.domain.User;
import com.example.creditbot.repository.UserRepository;
import com.example.creditbot.web.dto.AuthResponse;
import com.example.creditbot.web.dto.LoginRequest;
import com.example.creditbot.web.dto.RegisterRequest;
import com.example.creditbot.web.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует."
            );
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setManager(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return createResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неверный email или пароль."
                ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Неверный email или пароль."
            );
        }

        return createResponse(user);
    }

    private AuthResponse createResponse(User user) {
        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.isManager()
        );
    }
}