package com.example.role_permission.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "Email is Mandatory")
    @Email(message = "Email is not formatted")
    String email;
    @NotBlank(message = "Password is Mandatory")
    @Size(min = 8 , message = "Password should be 8 characters minimum")
    String password;
}
