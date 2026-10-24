package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((auth) ->
                    auth.requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

//                    .requestMatchers(HttpMethod.POST, "/api/v1/packages/**")
//                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
//
//                    .requestMatchers(HttpMethod.PUT, "/api/v1/packages/**")
//                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
//
//                    .requestMatchers(HttpMethod.DELETE, "/api/v1/packages/**")
//                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
