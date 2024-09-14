package com.example.role_permission.token;

import com.example.role_permission.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Token {
    @Id @GeneratedValue
    Integer id;

    @Enumerated(EnumType.STRING)
    TokenType tokenType;
    @Column(length = 1024)
    String token;

    boolean expired;
    boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
