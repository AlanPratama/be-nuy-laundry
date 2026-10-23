package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final Key key;

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    public JwtServiceImpl(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // =========================
    // GENERATE TOKEN
    // =========================
    @Override
    public String generateAccessToken(UserModel userModel, String sessionId) {
        return buildToken(
                userModel.getUsername(),
                userModel.getId(),
                userModel.getRole(),
                sessionId,
                accessTokenExpiration
        );
    }

    @Override
    public String generateRefreshToken(UserModel userModel, String sessionId) {
        return buildToken(
                userModel.getUsername(),
                userModel.getId(),
                userModel.getRole(),
                sessionId,
                refreshTokenExpiration
        );
    }

    @Override
    public String generateAccessTokenFromSession(UserSessionModel session) {
        return buildTokenFromSession(session, accessTokenExpiration);
    }

    @Override
    public String generateRefreshTokenFromSession(UserSessionModel session) {
        return buildTokenFromSession(session, refreshTokenExpiration);
    }

    // =========================
    // EXTRACT CLAIMS
    // =========================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(cleanToken(token))
                .getBody();
    }

    @Override
    public String extractRole(String token) {
        return this.extractAllClaims(token).get("role").toString();
    }

    @Override
    public String extractSessionId(String token) {
        return this.extractAllClaims(token).get("sessionId").toString();
    }

    @Override
    public String extractUsername(String token) {
        return this.extractAllClaims(token).getSubject();
    }

    // =========================
    // VALIDATION
    // =========================

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = this.extractAllClaims(token);

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // =========================
    // HELPER
    // =========================
    private String cleanToken(String token) {
        if (token == null)
            return null;

        if (token.startsWith("Bearer"))
            return token.substring(7);

        return token;
    }

    private String buildToken(String username, String userId, UserRole role, String sessionId, long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String buildTokenFromSession(UserSessionModel session, long expiration) {
        return Jwts.builder()
                .setSubject(session.getUser().getUsername())
                .claim("userId", session.getUser().getId())
                .claim("role", session.getUser().getRole())
                .claim("sessionId", session.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


}
