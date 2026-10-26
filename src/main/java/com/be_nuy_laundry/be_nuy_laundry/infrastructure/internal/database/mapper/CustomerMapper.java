package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.CustomerEntity;

import java.util.ArrayList;
import java.util.List;

public class CustomerMapper {

    public static CustomerEntity mapToEntity(CustomerModel model) {
        if (model == null)
            return null;

        CustomerEntity entity = new CustomerEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setPhone(model.getPhone());
        entity.setAddress(model.getAddress());
        entity.setTotalCucianKg(model.getTotalCucianKg() != null ? model.getTotalCucianKg() : java.math.BigDecimal.ZERO);
        entity.setTotalCucianSatuan(model.getTotalCucianSatuan() != null ? model.getTotalCucianSatuan() : 0);
        entity.setTotalTransaksi(model.getTotalTransaksi() != null ? model.getTotalTransaksi() : 0);
        entity.setLastOrder(model.getLastOrder());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        entity.setTransactions(TransactionMapper.mapToEntityList(model.getTransactions()));

        return entity;
    }

    public static List<CustomerEntity> mapToEntityList(List<CustomerModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(CustomerMapper::mapToEntity)
                .toList();
    }

    public static CustomerModel mapToDomain(CustomerEntity entity) {
        if (entity == null)
            return null;

        CustomerModel model = new CustomerModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setPhone(entity.getPhone());
        model.setAddress(entity.getAddress());
        model.setTotalCucianKg(entity.getTotalCucianKg() != null ? entity.getTotalCucianKg() : java.math.BigDecimal.ZERO);
        model.setTotalCucianSatuan(entity.getTotalCucianSatuan() != null ? entity.getTotalCucianSatuan() : 0);
        model.setTotalTransaksi(entity.getTotalTransaksi() != null ? entity.getTotalTransaksi() : 0);
        model.setLastOrder(entity.getLastOrder());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        return model;
    }

    public static List<CustomerModel> mapToDomainList(List<CustomerEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(CustomerMapper::mapToDomain)
                .toList();
    }

}
