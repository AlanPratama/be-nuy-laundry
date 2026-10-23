package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionFileEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class TransactionMapper {

    public static TransactionEntity mapToEntity(TransactionModel model) {
        if (model == null)
            return null;

        TransactionEntity entity = new TransactionEntity();
        entity.setId(model.getId());
        entity.setTotalPrice(model.getTotalPrice());
        entity.setInvoiceNo(model.getInvoiceNo());
        entity.setOrderDate(model.getOrderDate());
        entity.setTargetDate(model.getTargetDate());
        entity.setStatus(model.getStatus());
        entity.setCustomerName(model.getCustomerName());
        entity.setCustomerPhone(model.getCustomerPhone());
        entity.setCustomerAddress(model.getCustomerAddress());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        entity.setCustomer(CustomerMapper.mapToEntity(model.getCustomer()));
        entity.setUser(UserMapper.mapToEntity(model.getUser()));
        entity.setTransactionFileEntities(TransactionFileMapper.mapToEntityList(model.getTransactionFiles()));

        List<TransactionDetailEntity> details = TransactionDetailMapper.mapToEntityList(model.getTransactionDetails());
        entity.setTransactionDetails(details);

        List<TransactionFileEntity> files = TransactionFileMapper.mapToEntityList(model.getTransactionFiles());
        entity.setTransactionFileEntities(files);

        details.forEach(detail -> detail.setTransaction(entity));
        files.forEach(detail -> detail.setTransaction(entity));

        return entity;
    }

    public static List<TransactionEntity> mapToEntityList(List<TransactionModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(TransactionMapper::mapToEntity)
                .toList();
    }

    public static TransactionModel mapToDomain(TransactionEntity entity) {
        if (entity == null)
            return null;

        TransactionModel model = new TransactionModel();
        model.setId(entity.getId());
        model.setInvoiceNo(entity.getInvoiceNo());
        model.setTotalPrice(entity.getTotalPrice());
        model.setOrderDate(entity.getOrderDate());
        model.setTargetDate(entity.getTargetDate());
        model.setStatus(entity.getStatus());
        model.setCustomerName(entity.getCustomerName());
        model.setCustomerPhone(entity.getCustomerPhone());
        model.setCustomerAddress(entity.getCustomerAddress());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (ValueUtil.hasValueObject(entity.getCustomer()))
            model.setCustomer(CustomerMapper.mapToDomain(entity.getCustomer()));

        if (ValueUtil.hasValueObject(entity.getUser()))
            model.setUser(UserMapper.mapToDomain(entity.getUser()));

        if (ValueUtil.hasValueList(entity.getTransactionDetails()))
            model.setTransactionDetails(TransactionDetailMapper.mapToDomainList(entity.getTransactionDetails()));

        if (ValueUtil.hasValueList(entity.getTransactionFileEntities()))
            model.setTransactionFiles(TransactionFileMapper.mapToDomainList(entity.getTransactionFileEntities()));

        return model;
    }

    public static List<TransactionModel> mapToDomainList(List<TransactionEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(TransactionMapper::mapToDomain)
                .toList();
    }

    public static TransactionResponseDTO mapDomainToResponse(TransactionModel model) {
        if (model == null)
            return null;

        TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();
        transactionResponseDTO.setId(model.getId());
        transactionResponseDTO.setInvoiceNo(model.getInvoiceNo());
        transactionResponseDTO.setTotalPrice(model.getTotalPrice());
        transactionResponseDTO.setOrderDate(model.getOrderDate());
        transactionResponseDTO.setTargetDate(model.getTargetDate());
        transactionResponseDTO.setDeliveredDate(model.getDeliveredDate());
        transactionResponseDTO.setStatus(model.getStatus());
        transactionResponseDTO.setCustomerName(model.getCustomerName());
        transactionResponseDTO.setCustomerPhone(model.getCustomerPhone());
        transactionResponseDTO.setCustomerAddress(model.getCustomerAddress());
        transactionResponseDTO.setCreatedAt(model.getCreatedAt());
        transactionResponseDTO.setUpdatedAt(model.getUpdatedAt());

        transactionResponseDTO.setCustomer(model.getCustomer());
        transactionResponseDTO.setUser(UserMapper.mapDomainToResponse(model.getUser()));
        transactionResponseDTO.setTransactionDetails(model.getTransactionDetails());
        transactionResponseDTO.setTransactionFiles(model.getTransactionFiles());

        return transactionResponseDTO;
    }

    public static List<TransactionResponseDTO> mapDomainToResponseList(List<TransactionModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(TransactionMapper::mapDomainToResponse)
                .toList();
    }

}
