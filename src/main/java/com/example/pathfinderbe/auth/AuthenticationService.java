package com.example.pathfinderbe.auth;

import com.example.pathfinderbe.config.JwtService;
import com.example.pathfinderbe.exception.ApiResponse;
import com.example.pathfinderbe.user.Role;
import com.example.pathfinderbe.user.User;
import com.example.pathfinderbe.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public ApiResponse<AuthenticationResponse> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already in use");
        }

        User user = new User(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        user.setEnabled(false);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(24)
        );

        verificationTokenRepository.save(vt);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return ApiResponse.success(new AuthenticationResponse(null, null));
    }

    public ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            return ApiResponse.error("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!user.isEnabled()) {
            return ApiResponse.error("Email not verified");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ApiResponse.success(
                new AuthenticationResponse(accessToken, refreshToken)
        );
    }

    public ApiResponse<AuthenticationResponse> refreshToken(RefreshTokenRequest request) {

        try {
            String refreshToken = request.getRefreshToken();
            String userEmail = jwtService.extractUsername(refreshToken);

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow();

            if (!jwtService.isTokenValid(refreshToken, user)) {
                return ApiResponse.error("Invalid refresh token");
            }

            if (!jwtService.isRefreshToken(refreshToken)) {
                return ApiResponse.error("Not a refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return ApiResponse.success(
                    new AuthenticationResponse(newAccessToken, newRefreshToken)
            );

        } catch (ExpiredJwtException e) {
            return ApiResponse.error("Refresh token expired");
        } catch (JwtException e) {
            return ApiResponse.error("Invalid refresh token");
        }
    }

    @Transactional
    public ApiResponse<String> verifyEmail(String token) {

        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElse(null);

        if (vt == null) {
            return ApiResponse.error("Invalid verification token");
        }

        if (vt.isExpired()) {
            return ApiResponse.error("Verification token expired");
        }

        User user = vt.getUser();

        if (user.getPendingEmail() != null) {
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }

        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(vt);

        return ApiResponse.success("Email verified. You may now log in.");
    }
}
