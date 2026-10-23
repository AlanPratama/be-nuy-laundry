package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    @DisplayName("whenInstantiatingSecurityConfig_thenPasswordEncoderIsNotNull")
    void whenInstantiatingSecurityConfig_thenPasswordEncoderIsNotNull() {
        JwtAuthFilter filter = mock(JwtAuthFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isNotNull();
        assertThat(encoder.matches("password", encoder.encode("password"))).isTrue();
    }

    @Test
    @DisplayName("whenConfiguringSecurityFilterChain_thenBuildFilterChain")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void whenConfiguringSecurityFilterChain_thenBuildFilterChain() throws Exception {
        JwtAuthFilter filter = mock(JwtAuthFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            Customizer customizer = invocation.getArgument(0);
            AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry registry =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, RETURNS_DEEP_STUBS);
            customizer.customize(registry);
            return http;
        });
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(chain);

        SecurityFilterChain result = config.securityFilterChain(http);
        assertThat(result).isEqualTo(chain);
    }
}
