package com.example.role_permission.auth;

import com.example.role_permission.email.EmailService;
import com.example.role_permission.email.EmailTemplateName;
import com.example.role_permission.security.JwtService;
import com.example.role_permission.token.Token;
import com.example.role_permission.token.TokenRepository;
import com.example.role_permission.token.TokenType;
import com.example.role_permission.user.ActivationCode;
import com.example.role_permission.user.ActivationCodeRepository;
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
import java.util.List;

import static com.example.role_permission.token.TokenType.BEARER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationCodeRepository activationCodeRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

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
        var code = generateAndSaveActivationCode(user);
        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                confirmationUrl,
                code,
                "Account Activation"
        );
    }

    private String generateAndSaveActivationCode(User user) {
        var token = generateAndSaveActivationCode(6);
        var tokenBuild = ActivationCode
                .builder()
                .code(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15)).user(user)
                .build()
                ;
        activationCodeRepository.save(tokenBuild);
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
        revokeAllTokens(user);
        var token = Token
                .builder()
                .tokenType(BEARER)
                .token(jwtToken)
                .user(user)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build()
                ;
    }

    private void revokeAllTokens(User user) {
        List<Token> tokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if(tokens.isEmpty()) return;
        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    public void activateAccount(String code) throws MessagingException {
        ActivationCode tokenRepo = activationCodeRepository.findByCode(code).orElseThrow(
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
        activationCodeRepository.save(tokenRepo);
    }
}
