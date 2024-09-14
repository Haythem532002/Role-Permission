package com.example.role_permission.security;

import com.example.role_permission.token.Token;
import com.example.role_permission.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenRepository tokenRepository;
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader==null || !authHeader.startsWith("Bearer ")) return;
        String jwt = authHeader.substring(7);

        Token token = tokenRepository.findByToken(jwt).orElse(null);

        if(token!=null) {
            token.setRevoked(true);
            token.setExpired(true);
            tokenRepository.save(token);
        }
    }
}
