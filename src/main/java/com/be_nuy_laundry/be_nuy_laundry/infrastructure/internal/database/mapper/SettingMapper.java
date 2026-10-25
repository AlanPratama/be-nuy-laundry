package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.SettingEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;

public class SettingMapper {

    public static SettingEntity mapToEntity(SettingModel model) {
        if (model == null) return null;

        SettingEntity entity = new SettingEntity();
        entity.setId(model.getId());
        entity.setOutletName(model.getOutletName());
        entity.setAddress(model.getAddress());
        entity.setPhone(model.getPhone());
        entity.setOpenHour(model.getOpenHour());
        entity.setCloseHour(model.getCloseHour());
        entity.setFreeRadiusKm(model.getFreeRadiusKm());
        entity.setExtraCostPerKm(model.getExtraCostPerKm());
        entity.setReceiptFooterNote(model.getReceiptFooterNote());
        entity.setLogoUrl(model.getLogoUrl());
        entity.setLatitude(model.getLatitude());
        entity.setLongitude(model.getLongitude());
        entity.setGoogleMapsUrl(model.getGoogleMapsUrl());
        entity.setInstagram(model.getInstagram());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    public static SettingModel mapToDomain(SettingEntity entity) {
        if (entity == null) return null;

        SettingModel model = new SettingModel();
        model.setId(entity.getId());
        model.setOutletName(entity.getOutletName());
        model.setAddress(entity.getAddress());
        model.setPhone(entity.getPhone());
        model.setOpenHour(entity.getOpenHour());
        model.setCloseHour(entity.getCloseHour());
        model.setFreeRadiusKm(entity.getFreeRadiusKm());
        model.setExtraCostPerKm(entity.getExtraCostPerKm());
        model.setReceiptFooterNote(entity.getReceiptFooterNote());
        model.setLogoUrl(entity.getLogoUrl());
        model.setLatitude(entity.getLatitude());
        model.setLongitude(entity.getLongitude());
        model.setGoogleMapsUrl(entity.getGoogleMapsUrl());
        model.setInstagram(entity.getInstagram());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        return model;
    }

    public static SettingResponseDTO mapDomainToResponse(SettingModel model) {
        if (model == null) return null;

        return SettingResponseDTO.builder()
                .id(model.getId())
                .outletName(model.getOutletName())
                .address(model.getAddress())
                .phone(model.getPhone())
                .openHour(model.getOpenHour())
                .closeHour(model.getCloseHour())
                .freeRadiusKm(model.getFreeRadiusKm())
                .extraCostPerKm(model.getExtraCostPerKm())
                .receiptFooterNote(model.getReceiptFooterNote())
                .logoUrl(model.getLogoUrl())
                .latitude(model.getLatitude())
                .longitude(model.getLongitude())
                .googleMapsUrl(model.getGoogleMapsUrl())
                .instagram(model.getInstagram())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

}
