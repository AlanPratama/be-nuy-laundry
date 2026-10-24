package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionFileEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionFileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class TransactionFileMapper {

    public static TransactionFileEntity mapToEntity(TransactionFileModel model) {
        if (model == null)
            return null;

        TransactionFileEntity entity = new TransactionFileEntity();
        entity.setId(model.getId());
        entity.setFilePath(model.getFilePath());
        entity.setFileName(model.getFileName());
        entity.setContentType(model.getContentType());
        entity.setFileSize(model.getFileSize());
        entity.setFileType(model.getFileType());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        if (model.getTransaction() != null && model.getTransaction().getId() != null) {
            TransactionEntity transaction = new TransactionEntity();
            transaction.setId(model.getTransaction().getId());
            entity.setTransaction(transaction);
        }

        return entity;
    }

    public static List<TransactionFileEntity> mapToEntityList(List<TransactionFileModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(TransactionFileMapper::mapToEntity)
                .toList();
    }

    public static TransactionFileModel mapToDomain(TransactionFileEntity entity) {
        if (entity == null)
            return null;

        TransactionFileModel model = new TransactionFileModel();
        model.setId(entity.getId());
        model.setFilePath(entity.getFilePath());
        model.setFileName(entity.getFileName());
        model.setContentType(entity.getContentType());
        model.setFileSize(entity.getFileSize());
        model.setFileType(entity.getFileType());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

//        model.setTransaction(TransactionMapper.mapToDomain(entity.getTransaction()));

        return model;
    }

    public static List<TransactionFileModel> mapToDomainList(List<TransactionFileEntity> entities) {
        if (entities == null)
            return new ArrayList<>();


        return entities.stream()
                .map(TransactionFileMapper::mapToDomain)
                .toList();
    }

    public static TransactionFileModel mapMultipartFileToDomain(MultipartFile file, TransactionFileType transactionFileType, String filePath) {
        if (!ValueUtil.hasValueMultipartFile(file))
            return null;

        TransactionFileModel paymentReceiptTransactionFile = new TransactionFileModel();
        paymentReceiptTransactionFile.setFilePath(filePath);
        paymentReceiptTransactionFile.setFileName(file.getOriginalFilename());
        paymentReceiptTransactionFile.setContentType(file.getContentType());
        paymentReceiptTransactionFile.setFileSize(file.getSize());
        paymentReceiptTransactionFile.setFileType(transactionFileType);

        return paymentReceiptTransactionFile;
    }

}
