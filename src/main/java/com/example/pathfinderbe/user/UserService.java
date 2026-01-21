package com.example.pathfinderbe.user;

import com.example.pathfinderbe.auth.EmailService;
import com.example.pathfinderbe.auth.VerificationToken;
import com.example.pathfinderbe.auth.VerificationTokenRepository;
import com.example.pathfinderbe.dto.UserDto;
import com.example.pathfinderbe.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
        private final EmailService emailService;

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public UserDto getCurrentUserProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getId()
        );
    }
    public UserDto updateProfile(UpdateProfileRequest request, Authentication auth) {
        User user = getCurrentUser(auth);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return this.toDto(userRepository.save(user));
    }


    public ApiResponse<UserDto> updateEmail(UpdateEmailRequest request, Authentication auth) {

        if (!request.getNewEmail().equals(request.getConfirmNewEmail())) {
            return ApiResponse.error("Emails do not match");
        }

        if (userRepository.existsByEmail(request.getNewEmail())) {
            return ApiResponse.error("Email already in use");
        }

        User user = getCurrentUser(auth);

        user.setPendingEmail(request.getNewEmail());
        user.setEnabled(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(24)
        );

        verificationTokenRepository.save(vt);
        emailService.sendVerificationEmail(request.getNewEmail(), token);

        return ApiResponse.success(toDto(user));
    }


    public ApiResponse<UserDto> updatePassword(UpdatePasswordRequest request, Authentication auth) {

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ApiResponse.error("Passwords do not match");
        }

        User user = getCurrentUser(auth);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success(toDto(user));
    }

}
