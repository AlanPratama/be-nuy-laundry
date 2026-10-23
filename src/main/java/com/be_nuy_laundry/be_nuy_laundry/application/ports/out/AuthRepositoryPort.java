package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;

import java.util.Optional;

public interface AuthRepositoryPort {

    Optional<UserSessionModel> findById(String id);
    Optional<UserSessionModel> findByRefreshToken(String refreshToken);
    UserSessionModel save(UserSessionModel userSession);
    void deactiveAllSessionByUserId(String userId);
    void deactiveSession(String id);

}
