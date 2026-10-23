package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated;

import lombok.Getter;

@Getter
public enum TransactionFileType {
    PAYMENT_RECEIPT("Payment Receipt"),
    CONDITION_BEFORE("Condition Before"),
    CONDITION_AFTER("Confition After"),
    INVOICE_DOCUMENT("Invoice Document"),
    OTHER("Other");

    private final String displayName;

    TransactionFileType(String displayName) {
        this.displayName = displayName;
    }
}
