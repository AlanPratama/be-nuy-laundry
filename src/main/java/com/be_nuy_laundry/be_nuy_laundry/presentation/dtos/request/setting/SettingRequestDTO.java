package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.setting;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingRequestDTO {

    @NotBlank(message = "Nama outlet tidak boleh kosong")
    private String outletName;

    private String address;

    private String phone;

    private String openHour;

    private String closeHour;

    private Integer freeRadiusKm;

    private Integer extraCostPerKm;

    private String receiptFooterNote;

}
