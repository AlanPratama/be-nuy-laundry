package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated;

import lombok.Getter;

@Getter
public enum UserRole {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    STAFF("Staff");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }
}
