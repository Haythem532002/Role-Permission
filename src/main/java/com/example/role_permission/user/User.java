package com.example.role_permission.user;

import com.example.role_permission.token.Token;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @SuperBuilder
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Principal, UserDetails {
    @Id @GeneratedValue
    Integer id;
    String firstName;
    String lastName;
    @Column(unique = true)
    String email;
    String password;
    boolean enabled;
    boolean isLocked;

    @Enumerated(EnumType.STRING)
    Role role;

    @CreatedDate
    @Column(updatable = false,nullable = false)
    LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "user")
    List<Token> tokens;

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String fullName() {
        return firstName+" "+lastName;
    }
}
