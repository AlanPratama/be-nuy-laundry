package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.SettingModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.SettingEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting.SettingResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SettingMapperTest {

    @Test
    @DisplayName("mapToEntity - valid and null")
    void testMapToEntity() {
        new SettingMapper();

        assertThat(SettingMapper.mapToEntity(null)).isNull();

        SettingModel model = new SettingModel();
        model.setId("set-1");
        model.setOutletName("Nuy Laundry");
        model.setAddress("Jl. Melati");
        model.setPhone("08123");
        model.setOpenHour("07:00");
        model.setCloseHour("20:00");
        model.setFreeRadiusKm(5);
        model.setExtraCostPerKm(2500);
        model.setReceiptFooterNote("Note");
        model.setLogoUrl("/files/outlet/logo.png");
        model.setLatitude(-6.2000);
        model.setLongitude(106.8166);
        model.setGoogleMapsUrl("https://maps.google.com");
        model.setInstagram("@nuylaundry");
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());

        SettingEntity entity = SettingMapper.mapToEntity(model);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("set-1");
        assertThat(entity.getOutletName()).isEqualTo("Nuy Laundry");
        assertThat(entity.getAddress()).isEqualTo("Jl. Melati");
        assertThat(entity.getPhone()).isEqualTo("08123");
        assertThat(entity.getOpenHour()).isEqualTo("07:00");
        assertThat(entity.getCloseHour()).isEqualTo("20:00");
        assertThat(entity.getFreeRadiusKm()).isEqualTo(5);
        assertThat(entity.getExtraCostPerKm()).isEqualTo(2500);
        assertThat(entity.getReceiptFooterNote()).isEqualTo("Note");
        assertThat(entity.getLogoUrl()).isEqualTo("/files/outlet/logo.png");
        assertThat(entity.getLatitude()).isEqualTo(-6.2000);
        assertThat(entity.getLongitude()).isEqualTo(106.8166);
        assertThat(entity.getGoogleMapsUrl()).isEqualTo("https://maps.google.com");
        assertThat(entity.getInstagram()).isEqualTo("@nuylaundry");
    }

    @Test
    @DisplayName("mapToDomain - valid and null")
    void testMapToDomain() {
        assertThat(SettingMapper.mapToDomain(null)).isNull();

        SettingEntity entity = new SettingEntity();
        entity.setId("set-1");
        entity.setOutletName("Nuy Laundry");
        entity.setAddress("Jl. Melati");
        entity.setPhone("08123");
        entity.setOpenHour("07:00");
        entity.setCloseHour("20:00");
        entity.setFreeRadiusKm(5);
        entity.setExtraCostPerKm(2500);
        entity.setReceiptFooterNote("Note");
        entity.setLogoUrl("/files/outlet/logo.png");
        entity.setLatitude(-6.2000);
        entity.setLongitude(106.8166);
        entity.setGoogleMapsUrl("https://maps.google.com");
        entity.setInstagram("@nuylaundry");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        SettingModel model = SettingMapper.mapToDomain(entity);
        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo("set-1");
        assertThat(model.getOutletName()).isEqualTo("Nuy Laundry");
        assertThat(model.getAddress()).isEqualTo("Jl. Melati");
        assertThat(model.getPhone()).isEqualTo("08123");
        assertThat(model.getOpenHour()).isEqualTo("07:00");
        assertThat(model.getCloseHour()).isEqualTo("20:00");
        assertThat(model.getFreeRadiusKm()).isEqualTo(5);
        assertThat(model.getExtraCostPerKm()).isEqualTo(2500);
        assertThat(model.getReceiptFooterNote()).isEqualTo("Note");
        assertThat(model.getLogoUrl()).isEqualTo("/files/outlet/logo.png");
        assertThat(model.getLatitude()).isEqualTo(-6.2000);
        assertThat(model.getLongitude()).isEqualTo(106.8166);
        assertThat(model.getGoogleMapsUrl()).isEqualTo("https://maps.google.com");
        assertThat(model.getInstagram()).isEqualTo("@nuylaundry");
    }

    @Test
    @DisplayName("mapDomainToResponse - valid and null")
    void testMapDomainToResponse() {
        assertThat(SettingMapper.mapDomainToResponse(null)).isNull();

        SettingModel model = new SettingModel();
        model.setId("set-1");
        model.setOutletName("Nuy Laundry");
        model.setAddress("Jl. Melati");
        model.setPhone("08123");
        model.setOpenHour("07:00");
        model.setCloseHour("20:00");
        model.setFreeRadiusKm(5);
        model.setExtraCostPerKm(2500);
        model.setReceiptFooterNote("Note");
        model.setLogoUrl("/files/outlet/logo.png");
        model.setLatitude(-6.2000);
        model.setLongitude(106.8166);
        model.setGoogleMapsUrl("https://maps.google.com");
        model.setInstagram("@nuylaundry");
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());

        SettingResponseDTO dto = SettingMapper.mapDomainToResponse(model);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("set-1");
        assertThat(dto.getOutletName()).isEqualTo("Nuy Laundry");
        assertThat(dto.getAddress()).isEqualTo("Jl. Melati");
        assertThat(dto.getPhone()).isEqualTo("08123");
        assertThat(dto.getOpenHour()).isEqualTo("07:00");
        assertThat(dto.getCloseHour()).isEqualTo("20:00");
        assertThat(dto.getFreeRadiusKm()).isEqualTo(5);
        assertThat(dto.getExtraCostPerKm()).isEqualTo(2500);
        assertThat(dto.getReceiptFooterNote()).isEqualTo("Note");
        assertThat(dto.getLogoUrl()).isEqualTo("/files/outlet/logo.png");
        assertThat(dto.getLatitude()).isEqualTo(-6.2000);
        assertThat(dto.getLongitude()).isEqualTo(106.8166);
        assertThat(dto.getGoogleMapsUrl()).isEqualTo("https://maps.google.com");
        assertThat(dto.getInstagram()).isEqualTo("@nuylaundry");
    }
}
