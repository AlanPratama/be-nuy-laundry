package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID("Belum Bayar"),
    DOWN_PAYMENT("Uang Muka (DP)"),
    PAID("Lunas");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
