package com.example.annita.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.annotations.JdbcType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CONTRIBUTOR;

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false, name = "is_email_verified")
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(nullable = false, name = "receive_notifications")
    @Builder.Default
    private boolean receiveNotifications = true;

    @Column(nullable = false, name = "approved_event_count")
    @Builder.Default
    private int approvedEventCount = 0;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "company_nif", length = 20)
    private String companyNif;

    @Column(name = "company_phone", length = 20)
    private String companyPhone;

    @Column(name = "company_address", length = 300)
    private String companyAddress;

    @Column(name = "company_website", length = 200)
    private String companyWebsite;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
