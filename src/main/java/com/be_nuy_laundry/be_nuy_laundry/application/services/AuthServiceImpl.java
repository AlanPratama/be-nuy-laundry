package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.AuthUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.UserUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.AuthRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.SessionCacheService;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.UserSessionMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract.JwtService;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final AuthRepositoryPort authRepositoryPort;
    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionCacheService sessionCacheService;

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        UserModel userModel = userUseCase.getDomainUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), userModel.getPassword()))
            throw new ForbiddenException("Password tidak valid");

        if (!Boolean.TRUE.equals(userModel.getIsActive()))
            throw new ForbiddenException("Akun tidak aktif");

//        if (PARAMETER MULTI LOGIN AKTIF)
//            authRepositoryPort.deactiveAllSessionByUserId(userModel.getId());

        String sessionId = UUID.randomUUID().toString();

        String accessToken = jwtService.generateAccessToken(userModel, sessionId);
        String refreshToken = jwtService.generateRefreshToken(userModel, sessionId);

        UserSessionModel session = new UserSessionModel();
        session.setId(sessionId);
        session.setUser(userModel);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setIsActive(Boolean.TRUE);
        session.setDeviceInfo(request.getDeviceInfo());
        session.setExpiresAt(LocalDateTime.now().plusDays(7));

        UserSessionModel savedSession = authRepositoryPort.save(session);
        sessionCacheService.save(savedSession);

        return UserSessionMapper.mapDomainToAuthResponse(savedSession);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequest request) {
        UserSessionModel session = authRepositoryPort.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!session.getIsActive() || session.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new UnauthorizedException("Sesi telah berakhir");

        String accessToken = jwtService.generateAccessTokenFromSession(session);
        String refreshToken = jwtService.generateRefreshTokenFromSession(session);

        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);

        UserSessionModel savedSession = authRepositoryPort.save(session);
        sessionCacheService.save(savedSession);

        return UserSessionMapper.mapDomainToAuthResponse(savedSession);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        String sessionId = jwtService.extractSessionId(accessToken);

        UserSessionModel userSessionModel = authRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new UnauthorizedException("Sesi telah berakhir"));

        if (!userSessionModel.getIsActive())
            throw new UnauthorizedException("Sesi telah berakhir");

        authRepositoryPort.deactiveSession(sessionId);
        sessionCacheService.delete(sessionId);
    }

    @Override
    @Transactional
    public void logoutAll(String userId) {
        authRepositoryPort.deactiveAllSessionByUserId(userId);
    }

}
