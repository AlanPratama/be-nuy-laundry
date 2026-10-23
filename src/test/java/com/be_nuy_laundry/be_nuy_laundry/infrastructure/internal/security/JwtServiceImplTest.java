package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXPIRATION = 900000; // 15 mins
    private static final long REFRESH_EXPIRATION = 604800000; // 7 days

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("givenUserModel_whenGenerateAccessToken_thenReturnValidToken")
    void givenUserModel_whenGenerateAccessToken_thenReturnValidToken() {
        UserModel user = TestDataBuilder.createSampleUser();
        String sessionId = "sess-123";

        String token = jwtService.generateAccessToken(user, sessionId);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.extractRole(token)).isEqualTo(user.getRole().name());
        assertThat(jwtService.extractSessionId(token)).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("givenUserModel_whenGenerateRefreshToken_thenReturnValidToken")
    void givenUserModel_whenGenerateRefreshToken_thenReturnValidToken() {
        UserModel user = TestDataBuilder.createSampleUser();
        String sessionId = "sess-456";

        String token = jwtService.generateRefreshToken(user, sessionId);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.extractRole(token)).isEqualTo(user.getRole().name());
        assertThat(jwtService.extractSessionId(token)).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("givenUserSessionModel_whenGenerateTokensFromSession_thenReturnValidTokens")
    void givenUserSessionModel_whenGenerateTokensFromSession_thenReturnValidTokens() {
        UserSessionModel session = TestDataBuilder.createSampleSession();

        String accessToken = jwtService.generateAccessTokenFromSession(session);
        String refreshToken = jwtService.generateRefreshTokenFromSession(session);

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
        assertThat(jwtService.validateToken(accessToken)).isTrue();
        assertThat(jwtService.validateToken(refreshToken)).isTrue();
        assertThat(jwtService.extractUsername(accessToken)).isEqualTo(session.getUser().getUsername());
        assertThat(jwtService.extractSessionId(accessToken)).isEqualTo(session.getId());
    }

    @Test
    @DisplayName("givenTokenWithBearerPrefix_whenValidateAndExtract_thenHandleCleanTokenProperly")
    void givenTokenWithBearerPrefix_whenValidateAndExtract_thenHandleCleanTokenProperly() {
        UserModel user = TestDataBuilder.createSampleUser();
        String rawToken = jwtService.generateAccessToken(user, "sess-789");
        String bearerToken = "Bearer " + rawToken;

        assertThat(jwtService.validateToken(bearerToken)).isTrue();
        assertThat(jwtService.extractUsername(bearerToken)).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("givenExpiredToken_whenValidateToken_thenReturnFalse")
    void givenExpiredToken_whenValidateToken_thenReturnFalse() {
        // Service with negative expiration (-1 ms)
        JwtServiceImpl expiredJwtService = new JwtServiceImpl(SECRET, -1000L, -1000L);
        UserModel user = TestDataBuilder.createSampleUser();
        String expiredToken = expiredJwtService.generateAccessToken(user, "sess-expired");

        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("givenMalformedOrNullToken_whenValidateToken_thenReturnFalse")
    void givenMalformedOrNullToken_whenValidateToken_thenReturnFalse() {
        assertThat(jwtService.validateToken("invalid.jwt.token")).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
        assertThat(jwtService.validateToken(null)).isFalse();
    }
}
