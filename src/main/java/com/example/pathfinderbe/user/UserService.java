package com.example.pathfinderbe.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public User updateProfile(UpdateProfileRequest request, Authentication auth) {
        User user = getCurrentUser(auth);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userRepository.save(user);
    }


    public void updateEmail(UpdateEmailRequest request, Authentication auth) {
        if (!request.getNewEmail().equals(request.getConfirmNewEmail())) {
            throw new RuntimeException("Emails do not match");
        }

        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = getCurrentUser(auth);
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
    }


    public void updatePassword(UpdatePasswordRequest request, Authentication auth) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = getCurrentUser(auth);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
