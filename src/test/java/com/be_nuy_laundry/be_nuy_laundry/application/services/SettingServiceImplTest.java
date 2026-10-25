package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.SettingRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting.SettingRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingServiceImplTest {

    @Mock
    private SettingRepositoryPort settingRepositoryPort;

    @InjectMocks
    private SettingServiceImpl settingService;

    @Test
    @DisplayName("givenExistingSetting_whenGetSetting_thenReturnMappedResponse")
    void givenExistingSetting_whenGetSetting_thenReturnMappedResponse() {
        SettingModel model = SettingModel.builder()
                .outletName("Nuy Laundry")
                .address("Jl. Melati 12")
                .phone("08123456789")
                .openHour("07:00")
                .closeHour("21:00")
                .freeRadiusKm(5)
                .extraCostPerKm(2000)
                .receiptFooterNote("Bersih Wangi")
                .build();
        model.setId("set-1");

        when(settingRepositoryPort.getSetting()).thenReturn(Optional.of(model));

        SettingResponseDTO response = settingService.getSetting();

        assertThat(response).isNotNull();
        assertThat(response.getOutletName()).isEqualTo("Nuy Laundry");
        assertThat(response.getId()).isEqualTo("set-1");
        verify(settingRepositoryPort).getSetting();
    }

    @Test
    @DisplayName("givenNoSettingInDB_whenGetSetting_thenInitDefaultAndReturn")
    void givenNoSettingInDB_whenGetSetting_thenInitDefaultAndReturn() {
        SettingModel defaultModel = SettingModel.builder()
                .outletName("Nuy Laundry — Outlet Melati Raya")
                .address("Jl. Melati Raya No. 12, Jakarta Selatan 12345")
                .phone("0812-3456-7890")
                .openHour("07:00")
                .closeHour("21:00")
                .freeRadiusKm(5)
                .extraCostPerKm(2000)
                .receiptFooterNote("1 Mesin 1 Pelanggan • Pakaian Bersih, Wangi, & Rapi")
                .build();
        defaultModel.setId("default-1");

        when(settingRepositoryPort.getSetting()).thenReturn(Optional.empty());
        when(settingRepositoryPort.save(any(SettingModel.class))).thenReturn(defaultModel);

        SettingResponseDTO response = settingService.getSetting();

        assertThat(response).isNotNull();
        assertThat(response.getOutletName()).isEqualTo("Nuy Laundry — Outlet Melati Raya");
        verify(settingRepositoryPort).save(any(SettingModel.class));
    }

    @Test
    @DisplayName("givenRequest_whenUpdateSetting_thenUpdateAndReturnSaved")
    void givenRequest_whenUpdateSetting_thenUpdateAndReturnSaved() {
        SettingModel existing = SettingModel.builder()
                .outletName("Old Name")
                .address("Old Address")
                .build();
        existing.setId("set-1");

        SettingRequestDTO request = SettingRequestDTO.builder()
                .outletName("New Name")
                .address("New Address")
                .phone("0811223344")
                .openHour("08:00")
                .closeHour("22:00")
                .freeRadiusKm(10)
                .extraCostPerKm(3000)
                .receiptFooterNote("Updated Footer")
                .build();

        SettingModel updated = SettingModel.builder()
                .outletName("New Name")
                .address("New Address")
                .phone("0811223344")
                .openHour("08:00")
                .closeHour("22:00")
                .freeRadiusKm(10)
                .extraCostPerKm(3000)
                .receiptFooterNote("Updated Footer")
                .build();
        updated.setId("set-1");

        when(settingRepositoryPort.getSetting()).thenReturn(Optional.of(existing));
        when(settingRepositoryPort.save(any(SettingModel.class))).thenReturn(updated);

        SettingResponseDTO response = settingService.updateSetting(request);

        assertThat(response).isNotNull();
        assertThat(response.getOutletName()).isEqualTo("New Name");
        verify(settingRepositoryPort).save(existing);
    }
}
