package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.SettingEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.SettingJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingRepositoryAdapterTest {

    @Mock
    private SettingJpaRepository settingJpaRepository;

    @InjectMocks
    private SettingRepositoryAdapter settingRepositoryAdapter;

    @Test
    @DisplayName("givenModel_whenSave_thenReturnSavedDomainModel")
    void givenModel_whenSave_thenReturnSavedDomainModel() {
        SettingModel model = new SettingModel();
        model.setOutletName("Nuy Laundry Central");
        model.setAddress("Jl. Sudirman");
        model.setPhone("08123456789");
        model.setOpenHour("08:00");
        model.setCloseHour("21:00");
        model.setFreeRadiusKm(3);
        model.setExtraCostPerKm(2000);
        model.setReceiptFooterNote("Terima kasih");

        SettingEntity entity = new SettingEntity();
        entity.setId("set-1");
        entity.setOutletName("Nuy Laundry Central");

        when(settingJpaRepository.save(any(SettingEntity.class))).thenReturn(entity);

        SettingModel saved = settingRepositoryAdapter.save(model);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("set-1");
        assertThat(saved.getOutletName()).isEqualTo("Nuy Laundry Central");
        verify(settingJpaRepository).save(any(SettingEntity.class));
    }

    @Test
    @DisplayName("whenGetSetting_thenReturnDomainModelOrEmpty")
    void whenGetSetting_thenReturnDomainModelOrEmpty() {
        SettingEntity entity = new SettingEntity();
        entity.setId("set-1");
        entity.setOutletName("Nuy Laundry Central");

        when(settingJpaRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(entity));

        Optional<SettingModel> result = settingRepositoryAdapter.getSetting();
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("set-1");

        when(settingJpaRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());
        assertThat(settingRepositoryAdapter.getSetting()).isEmpty();
    }
}
