package com.example.role_permission.auth;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthenticationResponse {
    private String token;
}
