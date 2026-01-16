package com.example.pathfinderbe.user;

import com.example.pathfinderbe.auth.EmailService;
import com.example.pathfinderbe.auth.VerificationToken;
import com.example.pathfinderbe.auth.VerificationTokenRepository;
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

    public User updateProfile(UpdateProfileRequest request, Authentication auth) {
        User user = getCurrentUser(auth);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userRepository.save(user);
    }


    public User updateEmail(UpdateEmailRequest request, Authentication auth) {

        if (!request.getNewEmail().equals(request.getConfirmNewEmail())) {
            throw new RuntimeException("Emails do not match");
        }

        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = getCurrentUser(auth);

        user.setPendingEmail(request.getNewEmail());
        user.setEnabled(false); // lock account

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(24)
        );

        verificationTokenRepository.save(vt);

        emailService.sendVerificationEmail(request.getNewEmail(), token);

        return user;
    }

    public User updatePassword(UpdatePasswordRequest request, Authentication auth) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = getCurrentUser(auth);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userRepository.save(user);
    }
}
