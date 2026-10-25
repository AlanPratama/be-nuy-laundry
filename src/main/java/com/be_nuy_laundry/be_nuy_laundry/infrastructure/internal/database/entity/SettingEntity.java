package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
public class SettingEntity extends BaseEntity {

    @Column(name = "outlet_name", nullable = false)
    private String outletName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "open_hour", length = 10)
    private String openHour;

    @Column(name = "close_hour", length = 10)
    private String closeHour;

    @Column(name = "free_radius_km")
    private Integer freeRadiusKm;

    @Column(name = "extra_cost_per_km")
    private Integer extraCostPerKm;

    @Column(name = "receipt_footer_note", columnDefinition = "TEXT")
    private String receiptFooterNote;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "google_maps_url", columnDefinition = "TEXT")
    private String googleMapsUrl;

    @Column(name = "instagram", length = 255)
    private String instagram;
}
