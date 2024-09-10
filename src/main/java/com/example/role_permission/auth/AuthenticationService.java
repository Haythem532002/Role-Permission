package com.example.role_permission.auth;

import com.example.role_permission.email.EmailService;
import com.example.role_permission.email.EmailTemplateName;
import com.example.role_permission.security.JwtService;
import com.example.role_permission.user.Token;
import com.example.role_permission.user.TokenRepository;
import com.example.role_permission.user.User;
import com.example.role_permission.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.mailing.frontend.activation-url}")
    private String confirmationUrl;
    public void register(RegistrationRequest request) throws MessagingException {
        var user = User
                .builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdDate(LocalDateTime.now())
                .enabled(false)
                .isLocked(false)
                .build()
                ;
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                confirmationUrl,
                newToken,
                "Account Activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        var token = generateAndSaveActivationCode(6);
        var tokenBuild = Token
                .builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15)).user(user)
                .build()
                ;
        tokenRepository.save(tokenBuild);
        return token;
    }

    private String generateAndSaveActivationCode(int length) {
        String charachters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(charachters.length());
            codeBuilder.append(charachters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String,Object>();
        var user = ((User) auth.getPrincipal());
        var jwtToken = jwtService.buildToken(claims,user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build()
                ;
    }

    public void activateAccount(String token) throws MessagingException {
        Token tokenRepo = tokenRepository.findByToken(token).orElseThrow(
                () -> new RuntimeException("Invalid Token")
        );
        if(LocalDateTime.now().isAfter(tokenRepo.getExpiresAt())) {
            sendValidationEmail(tokenRepo.getUser());
            throw new RuntimeException("Token Expired , A new Token has been sent to you email");
        }
        User user = userRepository.findByEmail(tokenRepo.getUser().getEmail()).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found")
        );
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepo.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(tokenRepo);
    }
}
