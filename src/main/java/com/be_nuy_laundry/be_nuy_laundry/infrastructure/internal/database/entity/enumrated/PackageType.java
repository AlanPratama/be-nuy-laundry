package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated;

import lombok.Getter;

@Getter
public enum PackageType {
    PCS("PCS"),
    WEIGHT("Weight");

    private final String displayName;

    PackageType(String displayName) {
        this.displayName = displayName;
    }
}