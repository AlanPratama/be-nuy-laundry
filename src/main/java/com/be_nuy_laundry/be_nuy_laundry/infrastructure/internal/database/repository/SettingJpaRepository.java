package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingJpaRepository extends JpaRepository<SettingEntity, String> {

    Optional<SettingEntity> findFirstByOrderByCreatedAtAsc();

}
