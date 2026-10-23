package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserFilter {

    private String name;
    private String username;
    private String phone;
    private UserRole role;

}
