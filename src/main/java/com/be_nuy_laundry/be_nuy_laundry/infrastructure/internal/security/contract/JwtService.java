package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.security.contract;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;

public interface JwtService {

    String generateAccessToken(UserModel userModel, String sessionId);
    String generateRefreshToken(UserModel userModel, String sessionId);

    String generateAccessTokenFromSession(UserSessionModel userModel);
    String generateRefreshTokenFromSession(UserSessionModel userModel);

    String extractRole(String token);
    String extractSessionId(String token);
    String extractUsername(String token);

    boolean validateToken(String token);

}
