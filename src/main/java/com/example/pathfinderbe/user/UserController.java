package com.example.pathfinderbe.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updateProfile(request, authentication));
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(
            @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ) {
        userService.updateEmail(request, authentication);
        return ResponseEntity.ok("Email updated successfully");
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ) {
        userService.updatePassword(request, authentication);
        return ResponseEntity.ok("Password updated successfully");
    }
}