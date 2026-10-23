package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, String> {

    Optional<UserSessionEntity> findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query(value = "UPDATE user_sessions SET is_active = false WHERE user_id = :userId", nativeQuery = true)
    void deactiveAllSessionByUserId(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE user_sessions SET is_active = false WHERE id = :id", nativeQuery = true)
    void deactiveSession(@Param("id") String id);

}
