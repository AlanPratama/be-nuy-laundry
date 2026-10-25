package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettingModel extends BaseModel {
    private String outletName;
    private String address;
    private String phone;
    private String openHour;
    private String closeHour;
    private Integer freeRadiusKm;
    private Integer extraCostPerKm;
    private String receiptFooterNote;
    private String logoUrl;
    private Double latitude;
    private Double longitude;
    private String googleMapsUrl;
    private String instagram;
}
