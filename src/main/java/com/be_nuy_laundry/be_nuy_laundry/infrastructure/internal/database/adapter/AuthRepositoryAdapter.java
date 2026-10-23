package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.AuthRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserSessionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.UserSessionMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.UserSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthRepositoryAdapter implements AuthRepositoryPort {

    private final UserSessionJpaRepository userSessionJpaRepository;

    @Override
    public Optional<UserSessionModel> findById(String id) {
        return userSessionJpaRepository.findById(id)
                .map(UserSessionMapper::mapToDomain);
    }

    @Override
    public Optional<UserSessionModel> findByRefreshToken(String refreshToken) {
        return userSessionJpaRepository.findByRefreshToken(refreshToken)
                .map(UserSessionMapper::mapToDomain);
    }

    @Override
    public UserSessionModel save(UserSessionModel userSession) {
        UserSessionEntity entity = UserSessionMapper.mapToEntity(userSession);

        return UserSessionMapper.mapToDomain(userSessionJpaRepository.save(entity));
    }

    @Override
    public void deactiveAllSessionByUserId(String userId) {
        userSessionJpaRepository.deactiveAllSessionByUserId(userId);
    }

    @Override
    public void deactiveSession(String id) {
        userSessionJpaRepository.deactiveSession(id);
    }
}
