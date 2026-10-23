package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.PackageBenefitEntity;

import java.util.ArrayList;
import java.util.List;

public class PackageBenefitMapper {

    public static List<BenefitModel> getBenefitModelList(List<PackageBenefitEntity> packageBenefitEntities) {
        if (packageBenefitEntities == null)
            return new ArrayList<>();

        return packageBenefitEntities.stream()
                .map((data) -> BenefitMapper.mapToDomain(data.getBenefit()))
                .toList();
    }

}
