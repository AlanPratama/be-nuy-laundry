package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.AuthRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.SessionCacheService;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private SessionCacheService sessionCacheService;

    @Mock
    private AuthRepositoryPort authRepositoryPort;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("givenPublicPaths_whenShouldNotFilter_thenReturnTrue")
    void givenPublicPaths_whenShouldNotFilter_thenReturnTrue() {
        MockHttpServletRequest authRequest = new MockHttpServletRequest();
        authRequest.setServletPath("/api/auth/login");
        assertThat(jwtAuthFilter.shouldNotFilter(authRequest)).isTrue();

        MockHttpServletRequest publicRequest = new MockHttpServletRequest();
        publicRequest.setServletPath("/public/files/test.png");
        assertThat(jwtAuthFilter.shouldNotFilter(publicRequest)).isTrue();

        MockHttpServletRequest swaggerRequest = new MockHttpServletRequest();
        swaggerRequest.setServletPath("/swagger-ui/index.html");
        assertThat(jwtAuthFilter.shouldNotFilter(swaggerRequest)).isTrue();

        MockHttpServletRequest optionsRequest = new MockHttpServletRequest();
        optionsRequest.setMethod("OPTIONS");
        assertThat(jwtAuthFilter.shouldNotFilter(optionsRequest)).isTrue();

        MockHttpServletRequest healthRequest = new MockHttpServletRequest();
        healthRequest.setServletPath("/actuator/health");
        assertThat(jwtAuthFilter.shouldNotFilter(healthRequest)).isTrue();

        MockHttpServletRequest healthSubRequest = new MockHttpServletRequest();
        healthSubRequest.setServletPath("/actuator/health/readiness");
        assertThat(jwtAuthFilter.shouldNotFilter(healthSubRequest)).isTrue();

        MockHttpServletRequest protectedRequest = new MockHttpServletRequest();
        protectedRequest.setServletPath("/api/transactions");
        assertThat(jwtAuthFilter.shouldNotFilter(protectedRequest)).isFalse();
    }

    @Test
    @DisplayName("givenNoAuthorizationHeader_whenDoFilterInternal_thenContinueFilterChain")
    void givenNoAuthorizationHeader_whenDoFilterInternal_thenContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("givenInvalidBearerHeader_whenDoFilterInternal_thenContinueFilterChain")
    void givenInvalidBearerHeader_whenDoFilterInternal_thenContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("givenInvalidJwtToken_whenDoFilterInternal_thenReturnUnauthorized")
    void givenInvalidJwtToken_whenDoFilterInternal_thenReturnUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Invalid token");
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("givenSessionNotFoundInCacheOrDB_whenDoFilterInternal_thenReturnUnauthorized")
    void givenSessionNotFoundInCacheOrDB_whenDoFilterInternal_thenReturnUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn("sess-999");
        when(sessionCacheService.get("sess-999")).thenReturn(Optional.empty());
        when(authRepositoryPort.findById("sess-999")).thenReturn(Optional.empty());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Session not found");
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("givenSessionRevoked_whenDoFilterInternal_thenReturnUnauthorized")
    void givenSessionRevoked_whenDoFilterInternal_thenReturnUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserSessionModel session = TestDataBuilder.createSampleSession();
        session.setIsActive(false);

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn(session.getId());
        when(sessionCacheService.get(session.getId())).thenReturn(Optional.of(session));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Session revoked");
    }

    @Test
    @DisplayName("givenSessionExpired_whenDoFilterInternal_thenReturnUnauthorized")
    void givenSessionExpired_whenDoFilterInternal_thenReturnUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserSessionModel session = TestDataBuilder.createSampleSession();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn(session.getId());
        when(sessionCacheService.get(session.getId())).thenReturn(Optional.of(session));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Session expired");
    }

    @Test
    @DisplayName("givenValidSessionFromCache_whenDoFilterInternal_thenAuthenticateAndContinue")
    void givenValidSessionFromCache_whenDoFilterInternal_thenAuthenticateAndContinue() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserSessionModel session = TestDataBuilder.createSampleSession();

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn(session.getId());
        when(sessionCacheService.get(session.getId())).thenReturn(Optional.of(session));
        when(jwtService.extractUsername("validToken")).thenReturn("testuser");
        when(jwtService.extractRole("validToken")).thenReturn("ADMIN");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("givenCacheMissAndFoundInDB_whenDoFilterInternal_thenSaveToCacheAndAuthenticate")
    void givenCacheMissAndFoundInDB_whenDoFilterInternal_thenSaveToCacheAndAuthenticate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserSessionModel session = TestDataBuilder.createSampleSession();

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn(session.getId());
        when(sessionCacheService.get(session.getId())).thenReturn(Optional.empty());
        when(authRepositoryPort.findById(session.getId())).thenReturn(Optional.of(session));
        when(jwtService.extractUsername("validToken")).thenReturn("testuser");
        when(jwtService.extractRole("validToken")).thenReturn("ADMIN");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(sessionCacheService).save(session);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("givenUnexpectedException_whenDoFilterInternal_thenReturnUnauthorized")
    void givenUnexpectedException_whenDoFilterInternal_thenReturnUnauthorized() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.validateToken("validToken")).thenThrow(new RuntimeException("Unexpected parsing error"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }

    @Test
    @DisplayName("givenValidSessionWithNullExpiresAt_whenDoFilterInternal_thenAuthenticateAndContinue")
    void givenValidSessionWithNullExpiresAt_whenDoFilterInternal_thenAuthenticateAndContinue() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserSessionModel session = TestDataBuilder.createSampleSession();
        session.setExpiresAt(null);

        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractSessionId("validToken")).thenReturn(session.getId());
        when(sessionCacheService.get(session.getId())).thenReturn(Optional.of(session));
        when(jwtService.extractUsername("validToken")).thenReturn("testuser");
        when(jwtService.extractRole("validToken")).thenReturn("ADMIN");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
