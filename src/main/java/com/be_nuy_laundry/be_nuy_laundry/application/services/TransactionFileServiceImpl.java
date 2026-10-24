package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionFileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionFileRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionFileType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.TransactionFileMapper;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileUploadRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file.TransactionFileDeleteResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionFileServiceImpl implements TransactionFileUseCase {
    private final FileUseCase fileUseCase;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final TransactionFileRepositoryPort transactionFileRepositoryPort;

    @Override
    public TransactionFileModel getTransactionFileById(String id) {
        return transactionFileRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("File tidak ditemukan"));
    }

    @Override
    public List<TransactionFileModel> getTransactionFileByIds(List<String> ids) {
        List<String> distinctIds = ids.stream().distinct().toList();

        List<TransactionFileModel> transactionFileModels = transactionFileRepositoryPort.findByIds(distinctIds);

        if (distinctIds.size() != transactionFileModels.size())
            throw new DataNotFoundException("Beberapa file tidak ditemukan");

        return transactionFileModels;
    }

    @Override
    public List<TransactionFileModel> uploadTransactionFile(TransactionFileUploadRequestDTO request) {
        String invoiceNo = transactionRepositoryPort.getInvoiceNo(request.getTransactionId());

        if (!ValueUtil.hasValueString(invoiceNo))
            throw new DataNotFoundException("Transaksi tidak ditemukan");

        MultipartFile paymentReceiptFile = request.getPaymentReceiptFile();
        MultipartFile invoiceDocumentFile = request.getInvoiceDocumentFile();
        List<MultipartFile> conditionBeforeFiles = request.getConditionBeforeFiles();
        List<MultipartFile> conditionAfterFiles = request.getConditionAfterFiles();
        List<MultipartFile> otherFiles = request.getOtherFiles();

        List<TransactionFileModel> transactionFileModels = this.setTransactionFiles(
                invoiceNo, paymentReceiptFile,
                invoiceDocumentFile, conditionBeforeFiles,
                conditionAfterFiles, otherFiles);

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setId(request.getTransactionId());
        transactionModel.setInvoiceNo(invoiceNo);

        for (TransactionFileModel fileModel : transactionFileModels) {
            fileModel.setTransaction(transactionModel);
        }

        return transactionFileRepositoryPort.saveAll(transactionFileModels);
    }

    @Override
    @Transactional
    public List<TransactionFileDeleteResponseDTO> deleteListTransactionFile(TransactionFileDeleteRequestDTO request) {
        List<TransactionFileModel> transactionFileModels = this
                .getTransactionFileByIds(request.getTransactionFileIds());

        Map<String, TransactionFileModel> fileMap = new HashMap<>();
        List<String> fileNames = new ArrayList<>();

        for (TransactionFileModel transactionFileModel : transactionFileModels) {
            fileMap.put(transactionFileModel.getFilePath(), transactionFileModel);
            fileNames.add(transactionFileModel.getFilePath());
        }

        List<MinIoRemoveObjectErrorResponse> minIoErrorResponse = fileUseCase.deleteObjects(fileNames);

        List<TransactionFileDeleteResponseDTO> responses = new ArrayList<>(minIoErrorResponse.size());

        Set<String> failedObjectNames = new HashSet<>();

        for (MinIoRemoveObjectErrorResponse errorResponse : minIoErrorResponse) {
            String objectName = errorResponse.getObjectName();
            TransactionFileModel model = fileMap.get(objectName);

            TransactionFileDeleteResponseDTO res = new TransactionFileDeleteResponseDTO();
            res.setTransactionFile(model);
            res.setBucketName(errorResponse.getBucketName());
            res.setObjectName(objectName);
            res.setMessage(errorResponse.getMessage());
            responses.add(res);

            failedObjectNames.add(objectName);
        }

        List<String> successDeletedIds = transactionFileModels.stream()
                .filter((data) -> !failedObjectNames.contains(data.getFilePath()))
                .map(TransactionFileModel::getId)
                .toList();

        if (!successDeletedIds.isEmpty()) {
            transactionFileRepositoryPort.deleteByIds(successDeletedIds);
        }

        return responses;
    }

    @Override
    public List<TransactionFileDeleteResponseDTO> deleteListFile(TransactionFileDeleteRequestDTO request) {
        List<TransactionFileModel> transactionFileModels = this
                .getTransactionFileByIds(request.getTransactionFileIds());

        Map<String, TransactionFileModel> fileMap = new HashMap<>();
        List<String> fileNames = new ArrayList<>();

        for (TransactionFileModel transactionFileModel : transactionFileModels) {
            fileMap.put(transactionFileModel.getFilePath(), transactionFileModel);
            fileNames.add(transactionFileModel.getFilePath());
        }

        List<MinIoRemoveObjectErrorResponse> minIoErrorResponse = fileUseCase.deleteObjects(fileNames);

        List<TransactionFileDeleteResponseDTO> responses = new ArrayList<>(minIoErrorResponse.size());

        for (MinIoRemoveObjectErrorResponse errorResponse : minIoErrorResponse) {
            String objectName = errorResponse.getObjectName();
            TransactionFileModel model = fileMap.get(objectName);

            TransactionFileDeleteResponseDTO res = new TransactionFileDeleteResponseDTO();
            res.setTransactionFile(model);
            res.setBucketName(errorResponse.getBucketName());
            res.setObjectName(objectName);
            res.setMessage(errorResponse.getMessage());
            responses.add(res);
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteTransactionFile(String id) {
        TransactionFileModel existingModel = this.getTransactionFileById(id);

        fileUseCase.delete(existingModel.getFilePath());

        transactionFileRepositoryPort.deleteById(existingModel.getId());
    }

    @Override
    public List<TransactionFileModel> setTransactionFiles(
            String invoiceNo,
            MultipartFile paymentReceiptFile,
            MultipartFile invoiceDocumentFile,
            List<MultipartFile> conditionBeforeFiles,
            List<MultipartFile> conditionAfterFiles,
            List<MultipartFile> otherFiles) {
        List<TransactionFileModel> transactionFileModels = new ArrayList<>();

        if (ValueUtil.hasValueMultipartFile(paymentReceiptFile))
            transactionFileModels.add(this.setAndUploadTransactionFile(paymentReceiptFile,
                    TransactionFileType.PAYMENT_RECEIPT, invoiceNo));

        if (ValueUtil.hasValueMultipartFile(invoiceDocumentFile))
            transactionFileModels.add(this.setAndUploadTransactionFile(invoiceDocumentFile,
                    TransactionFileType.INVOICE_DOCUMENT, invoiceNo));

        if (ValueUtil.hasValueList(conditionBeforeFiles))
            for (MultipartFile conditionBeforeFile : conditionBeforeFiles)
                if (ValueUtil.hasValueMultipartFile(conditionBeforeFile))
                    transactionFileModels.add(this.setAndUploadTransactionFile(conditionBeforeFile,
                            TransactionFileType.CONDITION_BEFORE, invoiceNo));

        if (ValueUtil.hasValueList(conditionAfterFiles))
            for (MultipartFile conditionAfterFile : conditionAfterFiles)
                if (ValueUtil.hasValueMultipartFile(conditionAfterFile))
                    transactionFileModels.add(this.setAndUploadTransactionFile(conditionAfterFile,
                            TransactionFileType.CONDITION_AFTER, invoiceNo));

        if (ValueUtil.hasValueList(otherFiles))
            for (MultipartFile otherFile : otherFiles)
                if (ValueUtil.hasValueMultipartFile(otherFile))
                    transactionFileModels
                            .add(this.setAndUploadTransactionFile(otherFile, TransactionFileType.OTHER, invoiceNo));

        return transactionFileModels;
    }

    private TransactionFileModel setAndUploadTransactionFile(
            MultipartFile file, TransactionFileType transactionFileType, String invoiceNo) {
        String folder = MinIoConstant.FOLDER.TRANSACTION + "/" + invoiceNo + "/" + transactionFileType.getDisplayName();

        String filePath = fileUseCase.upload(
                file, file.getOriginalFilename(), folder);

        TransactionFileModel paymentReceiptTransactionFile = TransactionFileMapper
                .mapMultipartFileToDomain(file, transactionFileType, filePath);

        return paymentReceiptTransactionFile;
    }
}
