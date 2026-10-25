package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting.SettingRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;

public interface SettingUseCase {

    SettingResponseDTO getSetting();

    SettingResponseDTO updateSetting(SettingRequestDTO request);

}
