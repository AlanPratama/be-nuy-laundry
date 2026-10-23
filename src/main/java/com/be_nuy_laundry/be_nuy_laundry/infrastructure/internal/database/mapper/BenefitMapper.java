package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.BenefitEntity;

import java.util.ArrayList;
import java.util.List;

public class BenefitMapper {

    public static BenefitEntity mapToEntity(BenefitModel benefit) {
        if (benefit == null)
            return null;

        BenefitEntity entity = new BenefitEntity();
        entity.setId(benefit.getId());
        entity.setName(benefit.getName());
        entity.setCreatedAt(benefit.getCreatedAt());
        entity.setUpdatedAt(benefit.getUpdatedAt());

        return entity;
    }

    public static List<BenefitEntity> mapToEntityList(List<BenefitModel> benefits) {
        if (benefits == null)
            return new ArrayList<>();

        return benefits.stream()
                .map(BenefitMapper::mapToEntity)
                .toList();
    }

    public static BenefitModel mapToDomain(BenefitEntity benefitEntity) {
        if (benefitEntity == null)
            return null;

        BenefitModel benefit = new BenefitModel();
        benefit.setId(benefitEntity.getId());
        benefit.setName(benefitEntity.getName());
        benefit.setCreatedAt(benefitEntity.getCreatedAt());
        benefit.setUpdatedAt(benefitEntity.getUpdatedAt());

        return benefit;
    }

    public static List<BenefitModel> mapToDomainList(List<BenefitEntity> benefitEntities) {
        if (benefitEntities == null)
            return new ArrayList<>();

        return benefitEntities.stream()
                .map(BenefitMapper::mapToDomain)
                .toList();
    }

}
