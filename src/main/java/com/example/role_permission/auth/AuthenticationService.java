package com.example.role_permission.auth;

import com.example.role_permission.user.User;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    public void register(RegistrationRequest request) {
        var user = User
                .builder()

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return null;
    }

    public void activateAccount(String token) {
    }
}
