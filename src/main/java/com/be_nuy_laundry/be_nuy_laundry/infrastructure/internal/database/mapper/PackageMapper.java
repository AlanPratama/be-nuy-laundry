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

//        if (ValueUtil.hasValueList(entity.getBenefits()))
//            pkg.setBenefits(PackageBenefitMapper.getBenefitModelList(entity.getBenefits()));
//
//        if (Hibernate.isInitialized(entity.getTransactionDetails()) && ValueUtil.hasValueList(entity.getTransactionDetails()))
//            pkg.setTransactionDetails(TransactionDetailMapper.mapToDomainList(entity.getTransactionDetails()));

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
