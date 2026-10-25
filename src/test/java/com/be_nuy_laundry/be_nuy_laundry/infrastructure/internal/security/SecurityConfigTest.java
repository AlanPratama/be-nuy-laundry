package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private JwtAuthFilter createFilter() {
        return new JwtAuthFilter(null, null, null);
    }

    @Test
    @DisplayName("whenInstantiatingSecurityConfig_thenPasswordEncoderIsNotNull")
    void whenInstantiatingSecurityConfig_thenPasswordEncoderIsNotNull() {
        SecurityConfig config = new SecurityConfig(createFilter());

        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isNotNull();
        assertThat(encoder.matches("password", encoder.encode("password"))).isTrue();
    }

    @Test
    @DisplayName("whenInstantiatingSecurityConfig_thenCorsConfigurationSourceIsNotNull")
    void whenInstantiatingSecurityConfig_thenCorsConfigurationSourceIsNotNull() {
        SecurityConfig config = new SecurityConfig(createFilter());

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertThat(source).isNotNull();
    }

    @Test
    @DisplayName("whenRequestingCorsConfiguration_thenValidateOriginMethodsAndCredentials")
    void whenRequestingCorsConfiguration_thenValidateOriginMethodsAndCredentials() {
        SecurityConfig config = new SecurityConfig(createFilter());
        CorsConfigurationSource source = config.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/transactions");
        request.setMethod("POST");

        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowCredentials()).isTrue();
        assertThat(corsConfig.getAllowedOriginPatterns()).contains("*");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
    }
}
