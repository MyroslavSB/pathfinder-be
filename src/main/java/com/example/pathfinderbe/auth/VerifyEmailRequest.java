package com.example.pathfinderbe.auth;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }
}
