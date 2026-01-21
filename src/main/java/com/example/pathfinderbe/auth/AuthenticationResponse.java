package com.example.pathfinderbe.auth;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class AuthenticationResponse {

    private String message;

    public AuthenticationResponse(String message) {
       this.message = message;
    }
    
    public String getMessage(){
        return message;
    }
}