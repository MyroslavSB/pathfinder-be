package com.example.pathfinderbe.auth;

import com.example.pathfinderbe.config.JwtService;
import com.example.pathfinderbe.exception.ApiResponse;
import com.example.pathfinderbe.user.Role;
import com.example.pathfinderbe.user.User;
import com.example.pathfinderbe.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseCookie;

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

        return ApiResponse.success(new AuthenticationResponse());
    }

    public ApiResponse<AuthenticationResponse> authenticate(
            AuthenticationRequest request,
            HttpServletResponse response
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!user.isEnabled()) {
            return ApiResponse.error("Email not verified");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        setCookie(response, "access_token", accessToken, 60 * 60);
        setCookie(response, "refresh_token", refreshToken, 7 * 24 * 60 * 60);

        return ApiResponse.success(
                new AuthenticationResponse("Logged in successfully")
        );
    }


    public ApiResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = getCookie(request, "refresh_token");

        if (refreshToken == null) {
            return ApiResponse.error("No refresh token");
        }

        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        if (!jwtService.isTokenValid(refreshToken, user)
                || !jwtService.isRefreshToken(refreshToken)) {
            return ApiResponse.error("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        setCookie(response, "access_token", newAccessToken, 60 * 60);
        setCookie(response, "refresh_token", newRefreshToken, 7 * 24 * 60 * 60);

        return ApiResponse.success(
                new AuthenticationResponse("Token refreshed")
        );
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


    private void setCookie(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAgeSeconds) {

        ResponseCookie accessCookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("None") // Maybe .sameSite("Strict") if same domain
                .build();

        response.addHeader(
                "Set-Cookie",
                accessCookie.toString()
        );
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
