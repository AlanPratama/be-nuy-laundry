package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.SettingUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting.SettingRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/outlet/profile", "/api/v1/settings"})
@RequiredArgsConstructor
@Validated
public class SettingController {

    private final SettingUseCase settingUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";

    @GetMapping
    public ResponseEntity<ApiResponse<SettingResponseDTO>> getSetting() {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, settingUseCase.getSetting());
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SettingResponseDTO>> updateSetting(@Valid @RequestBody SettingRequestDTO request) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, settingUseCase.updateSetting(request));
    }

}
