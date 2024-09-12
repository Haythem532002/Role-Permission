package com.example.role_permission.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivationCode {
    @GeneratedValue @Id
    Integer id;

    @Column(unique = true)
    String code;
    LocalDateTime createdAt;
    LocalDateTime expiresAt;
    LocalDateTime validatedAt;

    @ManyToOne
    @JoinColumn(name = "userId",nullable = false)
    User user;


}
