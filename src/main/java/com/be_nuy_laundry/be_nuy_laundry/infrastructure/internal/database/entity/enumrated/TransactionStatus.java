package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    CANCELED("Canceled"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }
}
