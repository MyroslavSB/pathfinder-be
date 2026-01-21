package com.example.pathfinderbe.user;
import lombok.Data;

@Data
public class UpdateEmailRequest {
    private String newEmail;
    private String confirmNewEmail;
}