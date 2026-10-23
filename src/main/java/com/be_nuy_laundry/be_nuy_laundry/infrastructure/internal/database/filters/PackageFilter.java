package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PackageFilter {

    private String name;
    private String description;
    private PackageType type;

}
