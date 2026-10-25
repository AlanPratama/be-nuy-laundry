package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.SettingRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.SettingEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.SettingMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.SettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SettingRepositoryAdapter implements SettingRepositoryPort {

    private final SettingJpaRepository settingJpaRepository;

    @Override
    public SettingModel save(SettingModel model) {
        SettingEntity entity = SettingMapper.mapToEntity(model);
        SettingEntity saved = settingJpaRepository.save(entity);
        return SettingMapper.mapToDomain(saved);
    }

    @Override
    public Optional<SettingModel> getSetting() {
        return settingJpaRepository.findFirstByOrderByCreatedAtAsc()
                .map(SettingMapper::mapToDomain);
    }

}
