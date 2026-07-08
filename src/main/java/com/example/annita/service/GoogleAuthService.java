package com.example.annita.service;

import com.example.annita.dto.LoginResponse;
import com.example.annita.model.User;
import com.example.annita.model.UserRole;
import com.example.annita.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RestClient restClient;
    private final String googleClientId;

    public GoogleAuthService(UserRepository userRepository, TokenService tokenService,
                             RestClient.Builder restClientBuilder,
                             @Value("${google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.restClient = restClientBuilder.build();
        this.googleClientId = googleClientId;
    }

    @SuppressWarnings("unchecked")
    public LoginResponse authenticate(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O login com Google não está configurado. Contacte o administrador.");
        }

        Map<String, Object> payload;
        try {
            payload = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O token do Google é inválido ou expirou.");
        }

        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O token do Google é inválido ou expirou.");
        }

        String aud = (String) payload.get("aud");
        String iss = (String) payload.get("iss");
        String googleId = (String) payload.get("sub");
        String email = (String) payload.get("email");

        List<String> validIssuers = List.of("https://accounts.google.com", "accounts.google.com");
        if (!validIssuers.contains(iss)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O token do Google é inválido ou expirou.");
        }

        if (!googleClientId.equals(aud)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O token do Google não pertence a esta aplicação.");
        }

        if (email == null || googleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível obter os dados da sua conta Google.");
        }

        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                user.setGoogleId(googleId);
                user = userRepository.save(user);
            } else {
                String username = email.split("@")[0];
                String baseUsername = username;
                int suffix = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + suffix;
                    suffix++;
                }

                user = User.builder()
                        .username(username)
                        .email(email)
                        .password("")
                        .role(UserRole.CONTRIBUTOR)
                        .googleId(googleId)
                        .isEmailVerified(true)
                        .build();
                user = userRepository.save(user);
            }
        }

        String token = tokenService.generateToken(user);
        return new LoginResponse(token);
    }
}
