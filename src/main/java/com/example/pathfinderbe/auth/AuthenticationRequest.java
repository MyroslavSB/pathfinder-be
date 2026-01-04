package com.example.pathfinderbe.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
