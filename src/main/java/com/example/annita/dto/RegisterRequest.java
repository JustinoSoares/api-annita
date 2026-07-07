package com.example.annita.dto;

import com.example.annita.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuário deve ter entre 3 e 50 caracteres")
    @Schema(description = "Username (unique)")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser um endereço válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    @Schema(description = "Email (unique)")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter pelo menos 6 caracteres")
    @Schema(description = "Password (min 6 characters)")
    private String password;

    @Schema(description = "Receive email notifications", defaultValue = "true")
    private boolean receiveNotifications = true;

    @Schema(description = "User role: CONTRIBUTOR (default) for regular users, COMPANY for organizations", defaultValue = "CONTRIBUTOR", allowableValues = {"CONTRIBUTOR", "COMPANY"})
    private UserRole role = UserRole.CONTRIBUTOR;

    @Schema(description = "Company name (required if role=COMPANY, ignored otherwise)", nullable = true)
    private String companyName;

    @Schema(description = "Company tax ID / NIF (only for COMPANY accounts)", nullable = true)
    private String companyNif;

    @Schema(description = "Company phone number (only for COMPANY accounts)", nullable = true)
    private String companyPhone;

    @Schema(description = "Company address (only for COMPANY accounts)", nullable = true)
    private String companyAddress;

    @Schema(description = "Company website (only for COMPANY accounts)", nullable = true)
    private String companyWebsite;
}
