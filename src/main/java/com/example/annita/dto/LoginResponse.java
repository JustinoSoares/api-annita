package com.example.annita.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private final String type = "Bearer";
    private String username;
    private String email;
    private String role;
}
