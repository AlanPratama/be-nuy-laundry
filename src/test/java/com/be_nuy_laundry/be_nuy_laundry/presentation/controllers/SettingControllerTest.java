package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.SettingUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting.SettingRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingControllerTest {

    @Mock
    private SettingUseCase settingUseCase;

    @InjectMocks
    private SettingController settingController;

    @Test
    @DisplayName("whenGetSetting_thenReturnSettingResponseDTO")
    void whenGetSetting_thenReturnSettingResponseDTO() {
        SettingResponseDTO responseDTO = SettingResponseDTO.builder()
                .id("set-1")
                .outletName("Nuy Laundry")
                .address("Jl. Melati No. 12")
                .phone("08123456789")
                .openHour("07:00")
                .closeHour("21:00")
                .freeRadiusKm(5)
                .extraCostPerKm(2000)
                .build();

        when(settingUseCase.getSetting()).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<SettingResponseDTO>> response = settingController.getSetting();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getOutletName()).isEqualTo("Nuy Laundry");
        verify(settingUseCase).getSetting();
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdateSetting_thenReturnUpdatedSettingResponseDTO")
    void givenValidRequest_whenUpdateSetting_thenReturnUpdatedSettingResponseDTO() {
        SettingRequestDTO request = SettingRequestDTO.builder()
                .outletName("Nuy Laundry Updated")
                .address("Jl. Melati No. 99")
                .phone("081299998888")
                .openHour("08:00")
                .closeHour("22:00")
                .freeRadiusKm(7)
                .extraCostPerKm(2500)
                .receiptFooterNote("Wangi & Rapi")
                .build();

        SettingResponseDTO updatedDTO = SettingResponseDTO.builder()
                .id("set-1")
                .outletName("Nuy Laundry Updated")
                .address("Jl. Melati No. 99")
                .phone("081299998888")
                .build();

        when(settingUseCase.updateSetting(any(SettingRequestDTO.class))).thenReturn(updatedDTO);

        ResponseEntity<ApiResponse<SettingResponseDTO>> response = settingController.updateSetting(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getOutletName()).isEqualTo("Nuy Laundry Updated");
        verify(settingUseCase).updateSetting(any(SettingRequestDTO.class));
    }
}
