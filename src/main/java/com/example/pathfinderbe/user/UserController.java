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
    public ResponseEntity<User> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updateProfile(request, authentication));
    }

    @PutMapping("/email")
    public ResponseEntity<User> updateEmail(
            @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(userService.updateEmail(request, authentication));
    }

    @PutMapping("/password")
    public ResponseEntity<User> updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updatePassword(request, authentication));
    }
}