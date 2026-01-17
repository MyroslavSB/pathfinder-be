package com.example.pathfinderbe.auth;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {

        String link = "https://asymptotically-symmetric-leatha.ngrok-free.dev/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your Pathfinder account");
        message.setText("""
                Welcome to Pathfinder!

                Click the link below to verify your account:

                %s

                This link expires in 24 hours.
                """.formatted(link));

        mailSender.send(message);
    }
}
