package com.example.role_permission.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.example.role_permission.user.Permission.*;
import static com.example.role_permission.user.Role.ADMIN;
import static com.example.role_permission.user.Role.MANAGER;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req
                                .requestMatchers(
                                        "/auth/**",
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html"
                                )
                                .permitAll()


                                .requestMatchers("/api/v1/manager/**")
                                .hasAnyRole(ADMIN.name(),MANAGER.name())
                                .requestMatchers(HttpMethod.GET,"/api/v1/manager/**")
                                .hasAnyAuthority(ADMIN_READ.name(),MANAGER_READ.name())
                                .requestMatchers(HttpMethod.POST,"/api/v1/manager/**")
                                .hasAnyAuthority(ADMIN_CREATE.name(),MANAGER_CREATE.name())
                                .requestMatchers(HttpMethod.PUT,"/api/v1/manager/**")
                                .hasAnyAuthority(ADMIN_UPDATE.name(),MANAGER_UPDATE.name())
                                .requestMatchers(HttpMethod.DELETE,"/api/v1/manager/**")
                                .hasAnyAuthority(ADMIN_DELETE.name(),MANAGER_DELETE.name())
                                .anyRequest()
                                .authenticated()
                        )
                .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                ;
        return http.build();
    }

}
