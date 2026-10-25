package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingResponseDTO {

    private String id;
    private String outletName;
    private String address;
    private String phone;
    private String openHour;
    private String closeHour;
    private Integer freeRadiusKm;
    private Integer extraCostPerKm;
    private String receiptFooterNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
