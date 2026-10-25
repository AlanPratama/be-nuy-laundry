package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.SettingUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.SettingRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.SettingMapper;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting.SettingRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingUseCase {

    private final SettingRepositoryPort settingRepositoryPort;

    @Override
    public SettingResponseDTO getSetting() {
        SettingModel model = settingRepositoryPort.getSetting()
                .orElseGet(this::initDefaultSetting);

        return SettingMapper.mapDomainToResponse(model);
    }

    @Override
    @Transactional
    public SettingResponseDTO updateSetting(SettingRequestDTO request) {
        SettingModel existing = settingRepositoryPort.getSetting()
                .orElseGet(this::initDefaultSetting);

        existing.setOutletName(request.getOutletName());
        existing.setAddress(request.getAddress());
        existing.setPhone(request.getPhone());
        existing.setOpenHour(request.getOpenHour());
        existing.setCloseHour(request.getCloseHour());
        existing.setFreeRadiusKm(request.getFreeRadiusKm());
        existing.setExtraCostPerKm(request.getExtraCostPerKm());
        existing.setReceiptFooterNote(request.getReceiptFooterNote());

        SettingModel saved = settingRepositoryPort.save(existing);
        return SettingMapper.mapDomainToResponse(saved);
    }

    private SettingModel initDefaultSetting() {
        SettingModel defaultSetting = SettingModel.builder()
                .outletName("Nuy Laundry — Outlet Melati Raya")
                .address("Jl. Melati Raya No. 12, Jakarta Selatan 12345")
                .phone("0812-3456-7890")
                .openHour("07:00")
                .closeHour("21:00")
                .freeRadiusKm(5)
                .extraCostPerKm(2000)
                .receiptFooterNote("1 Mesin 1 Pelanggan • Pakaian Bersih, Wangi, & Rapi")
                .build();

        return settingRepositoryPort.save(defaultSetting);
    }

}
