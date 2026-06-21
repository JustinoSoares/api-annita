package com.example.annita.dto;

import com.example.annita.model.User;
import com.example.annita.model.UserRole;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private UserRole role;
    private boolean isActive;
    private boolean isEmailVerified;
    private boolean receiveNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.isActive = user.isActive();
        this.isEmailVerified = user.isEmailVerified();
        this.receiveNotifications = user.isReceiveNotifications();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
