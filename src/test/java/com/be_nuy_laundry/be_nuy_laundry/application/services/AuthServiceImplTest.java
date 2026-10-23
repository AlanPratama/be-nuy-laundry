package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.UserUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.AuthRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.SessionCacheService;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract.JwtService;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthRepositoryPort authRepositoryPort;

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SessionCacheService sessionCacheService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("givenValidLoginRequest_whenLogin_thenReturnAuthResponseDTO")
    void givenValidLoginRequest_whenLogin_thenReturnAuthResponseDTO() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setDeviceInfo("Chrome on macOS");

        UserModel user = TestDataBuilder.createSampleUser();
        when(userUseCase.getDomainUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(eq(user), anyString())).thenReturn("mock.access.token");
        when(jwtService.generateRefreshToken(eq(user), anyString())).thenReturn("mock.refresh.token");
        when(authRepositoryPort.save(any(UserSessionModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("mock.refresh.token");
        assertThat(response.getDeviceInfo()).isEqualTo("Chrome on macOS");
        verify(sessionCacheService).save(any(UserSessionModel.class));
    }

    @Test
    @DisplayName("givenWrongPassword_whenLogin_thenThrowForbiddenException")
    void givenWrongPassword_whenLogin_thenThrowForbiddenException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        UserModel user = TestDataBuilder.createSampleUser();
        when(userUseCase.getDomainUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Password tidak valid");
    }

    @Test
    @DisplayName("givenInactiveUser_whenLogin_thenThrowForbiddenException")
    void givenInactiveUser_whenLogin_thenThrowForbiddenException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("inactiveuser");
        request.setPassword("password123");

        UserModel user = TestDataBuilder.createSampleUser();
        user.setIsActive(false);

        when(userUseCase.getDomainUserByUsername("inactiveuser")).thenReturn(user);
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Akun tidak aktif");
    }

    @Test
    @DisplayName("givenValidRefreshToken_whenRefreshToken_thenReturnNewTokens")
    void givenValidRefreshToken_whenRefreshToken_thenReturnNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid.refresh.token");

        UserSessionModel session = TestDataBuilder.createSampleSession();
        when(authRepositoryPort.findByRefreshToken("valid.refresh.token")).thenReturn(Optional.of(session));
        when(jwtService.generateAccessTokenFromSession(session)).thenReturn("new.access.token");
        when(jwtService.generateRefreshTokenFromSession(session)).thenReturn("new.refresh.token");
        when(authRepositoryPort.save(any(UserSessionModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
        verify(sessionCacheService).save(any(UserSessionModel.class));
    }

    @Test
    @DisplayName("givenNonExistentRefreshToken_whenRefreshToken_thenThrowUnauthorizedException")
    void givenNonExistentRefreshToken_whenRefreshToken_thenThrowUnauthorizedException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("nonexistent.token");

        when(authRepositoryPort.findByRefreshToken("nonexistent.token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("givenInactiveOrExpiredSession_whenRefreshToken_thenThrowUnauthorizedException")
    void givenInactiveOrExpiredSession_whenRefreshToken_thenThrowUnauthorizedException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revoked.token");

        UserSessionModel inactiveSession = TestDataBuilder.createSampleSession();
        inactiveSession.setIsActive(false);
        when(authRepositoryPort.findByRefreshToken("revoked.token")).thenReturn(Optional.of(inactiveSession));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Sesi telah berakhir");

        // Expired session
        UserSessionModel expiredSession = TestDataBuilder.createSampleSession();
        expiredSession.setIsActive(true);
        expiredSession.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(authRepositoryPort.findByRefreshToken("expired.token")).thenReturn(Optional.of(expiredSession));

        RefreshTokenRequest requestExpired = new RefreshTokenRequest();
        requestExpired.setRefreshToken("expired.token");

        assertThatThrownBy(() -> authService.refreshToken(requestExpired))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Sesi telah berakhir");
    }

    @Test
    @DisplayName("givenValidAccessToken_whenLogout_thenDeactivateAndEvictFromCache")
    void givenValidAccessToken_whenLogout_thenDeactivateAndEvictFromCache() {
        String token = "Bearer valid.token";
        String sessionId = "sess-123";
        UserSessionModel session = TestDataBuilder.createSampleSession();
        session.setId(sessionId);

        when(jwtService.extractSessionId(token)).thenReturn(sessionId);
        when(authRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        authService.logout(token);

        verify(authRepositoryPort).deactiveSession(sessionId);
        verify(sessionCacheService).delete(sessionId);
    }

    @Test
    @DisplayName("givenInactiveSession_whenLogout_thenThrowUnauthorizedException")
    void givenInactiveSession_whenLogout_thenThrowUnauthorizedException() {
        String token = "Bearer valid.token";
        String sessionId = "sess-revoked";
        UserSessionModel session = TestDataBuilder.createSampleSession();
        session.setId(sessionId);
        session.setIsActive(false);

        when(jwtService.extractSessionId(token)).thenReturn(sessionId);
        when(authRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Sesi telah berakhir");
    }

    @Test
    @DisplayName("givenNotFoundSession_whenLogout_thenThrowUnauthorizedException")
    void givenNotFoundSession_whenLogout_thenThrowUnauthorizedException() {
        String token = "Bearer valid.token";
        String sessionId = "sess-unknown";

        when(jwtService.extractSessionId(token)).thenReturn(sessionId);
        when(authRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Sesi telah berakhir");
    }

    @Test
    @DisplayName("givenUserId_whenLogoutAll_thenDeactivateAllSessionsByUserId")
    void givenUserId_whenLogoutAll_thenDeactivateAllSessionsByUserId() {
        String userId = "user-123";

        authService.logoutAll(userId);

        verify(authRepositoryPort).deactiveAllSessionByUserId(userId);
    }
}
