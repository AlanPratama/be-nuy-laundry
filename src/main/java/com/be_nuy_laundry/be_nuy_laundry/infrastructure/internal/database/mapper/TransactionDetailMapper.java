package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;

import java.util.ArrayList;
import java.util.List;

public class TransactionDetailMapper {

    public static TransactionDetailEntity mapToEntity(TransactionDetailModel model) {
        if (model == null)
            return null;

        TransactionDetailEntity entity = new TransactionDetailEntity();
        entity.setId(model.getId());
        entity.setQuantity(model.getQuantity());
        entity.setTotalPrice(model.getTotalPrice());
        entity.setPackageName(model.getPackageName());
        entity.setPackageDescription(model.getPackageDescription());
        entity.setPackageDaysEstimation(model.getPackageDaysEstimation());
        entity.setPackageType(model.getPackageType());
        entity.setTargetDate(model.getTargetDate());
        entity.setDeliveredDate(model.getDeliveredDate());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    public static List<TransactionDetailEntity> mapToEntityList(List<TransactionDetailModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(TransactionDetailMapper::mapToEntity)
                .toList();
    }

    public static TransactionDetailModel mapToDomain(TransactionDetailEntity entity) {
        if (entity == null)
            return null;

        TransactionDetailModel model = new TransactionDetailModel();
        model.setId(entity.getId());
        model.setQuantity(entity.getQuantity());
        model.setTotalPrice(entity.getTotalPrice());
        model.setPackageName(entity.getPackageName());
        model.setPackageDescription(entity.getPackageDescription());
        model.setPackageDaysEstimation(entity.getPackageDaysEstimation());
        model.setPackageType(entity.getPackageType());
        model.setTargetDate(entity.getTargetDate());
        model.setDeliveredDate(entity.getDeliveredDate());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getPkg() != null)
            model.setPackageId(entity.getPkg().getId());

        if (entity.getTransaction() != null)
            model.setTransactionId(entity.getTransaction().getId());

        return model;
    }

    public static List<TransactionDetailModel> mapToDomainList(List<TransactionDetailEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(TransactionDetailMapper::mapToDomain)
                .toList();
    }
}
