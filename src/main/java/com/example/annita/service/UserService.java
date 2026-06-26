package com.example.annita.service;

import com.example.annita.dto.*;
import com.example.annita.model.User;
import com.example.annita.repository.UserRepository;
import com.example.annita.repository.specification.UserSpecifications;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este nome de usuário já está em uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este email já está cadastrado");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(com.example.annita.model.UserRole.CONTRIBUTOR)
                .receiveNotifications(request.isReceiveNotifications())
                .build();

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nome de usuário ou senha inválidos"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta está inativa/bloqueada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nome de usuário ou senha inválidos");
        }

        String token = tokenService.generateToken(user);
        return new LoginResponse(token);
    }

    public void sendVerificationCode(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já foi verificado");
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public void verifyEmail(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já foi verificado");
        }

        if (user.getVerificationCode() == null || user.getVerificationCodeExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum código de verificação solicitado. Solicite um código primeiro.");
        }

        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação expirou. Solicite um novo código.");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido.");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public PageResponse<UserResponse> getUsers(String search, com.example.annita.model.UserRole role, Boolean isActive, int page, int perPage) {
        int pageIndex = Math.max(0, page - 1);
        int size = Math.max(1, perPage);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageIndex, size);

        org.springframework.data.domain.Page<User> usersPage = userRepository.findAll(UserSpecifications.filter(search, role, isActive), pageable);

        List<UserResponse> content = usersPage.getContent().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(usersPage, content);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return new UserResponse(user);
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este nome de usuário já está em uso");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este email já está cadastrado");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getReceiveNotifications() != null) {
            user.setReceiveNotifications(request.getReceiveNotifications());
        }

        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }
}
