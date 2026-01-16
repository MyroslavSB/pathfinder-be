package com.example.pathfinderbe.auth;

import com.example.pathfinderbe.config.JwtService;
import com.example.pathfinderbe.user.Role;
import com.example.pathfinderbe.user.User;
import com.example.pathfinderbe.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public AuthenticationResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
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

        return new AuthenticationResponse(null, null); // no JWT yet
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            String userEmail = jwtService.extractUsername(refreshToken);

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Must be a valid refresh token
            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new JwtException("Invalid refresh token");
            }

            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new JwtException("Not a refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return new AuthenticationResponse(newAccessToken, newRefreshToken);

        } catch (ExpiredJwtException e) {
            throw new JwtException("Refresh token expired");
        }
    }

    @Transactional
    public void verifyEmail(String token) {

        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (vt.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        User user = vt.getUser();

        if (user.getPendingEmail() != null) {
            user.setEmail(user.getPendingEmail());
            user.setPendingEmail(null);
        }

        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(vt);
    }


}
