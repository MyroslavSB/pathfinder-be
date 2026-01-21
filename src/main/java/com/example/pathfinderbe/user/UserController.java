package com.example.pathfinderbe.user;

import com.example.pathfinderbe.dto.UserDto;
import com.example.pathfinderbe.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/self")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getCurrentUserProfile(authentication))
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.updateProfile(request, authentication))
        );
    }

    @PutMapping("/email")
    public ResponseEntity<ApiResponse<UserDto>> updateEmail(
            @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ) {
        ApiResponse<UserDto> response = userService.updateEmail(request, authentication);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<UserDto>> updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication
    ) {
        ApiResponse<UserDto> response = userService.updatePassword(request, authentication);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
