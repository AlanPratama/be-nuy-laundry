package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.PackageEntity;

import java.util.ArrayList;
import java.util.List;

public class PackageMapper {

    public static PackageEntity mapToEntity(PackageModel domain) {
        if (domain == null)
            return null;

        PackageEntity entity = new PackageEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType());
        entity.setPrice(domain.getPrice());
        entity.setDaysEstimation(domain.getDaysEstimation());
        entity.setPhoto(domain.getPhoto());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }

    public static List<PackageEntity> mapToEntityList(List<PackageModel> domains) {
        if (domains == null)
            return new ArrayList<>();

        return domains.stream()
                .map(PackageMapper::mapToEntity)
                .toList();
    }

    public static PackageModel mapToDomain(PackageEntity entity) {
        if (entity == null)
            return null;

        PackageModel pkg = new PackageModel();
        pkg.setId(entity.getId());
        pkg.setName(entity.getName());
        pkg.setDescription(entity.getDescription());
        pkg.setType(entity.getType());
        pkg.setPrice(entity.getPrice());
        pkg.setDaysEstimation(entity.getDaysEstimation());
        pkg.setPhoto(entity.getPhoto());
        pkg.setCreatedAt(entity.getCreatedAt());
        pkg.setUpdatedAt(entity.getUpdatedAt());

        if (org.hibernate.Hibernate.isInitialized(entity.getBenefits()) && com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil.hasValueList(entity.getBenefits())) {
            List<com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel> benefitModels = PackageBenefitMapper.getBenefitModelList(entity.getBenefits());
            java.util.Map<String, com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel> distinctMap = new java.util.LinkedHashMap<>();
            for (com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel bm : benefitModels) {
                if (bm != null && bm.getId() != null) {
                    distinctMap.putIfAbsent(bm.getId(), bm);
                }
            }
            pkg.setBenefits(new java.util.ArrayList<>(distinctMap.values()));
            pkg.setBenefitIds(new java.util.ArrayList<>(distinctMap.keySet()));
        }

        return pkg;
    }

    public static List<PackageModel> mapToDomainList(List<PackageEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(PackageMapper::mapToDomain)
                .toList();
    }
}
