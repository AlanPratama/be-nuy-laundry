package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.*;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.*;
import com.be_nuy_laundry.be_nuy_laundry.domain.utils.TransactionNumberGenerator;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.TransactionMapper;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.detail.TransactionDetailRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final PackageUseCase packageUseCase;
    private final CustomerUseCase customerUseCase;
    private final UserUseCase userUseCase;
    private final TransactionFileUseCase transactionFileUseCase;
    private final TransactionDetailUseCase transactionDetailUseCase;

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest) {
        String invoiceNo = TransactionNumberGenerator.generateInvoiceNo();
        List<String> packageIds = transactionRequest.getTransactionDetails().stream().map(TransactionDetailRequestDTO::getPackageId).toList();
        Map<String, PackageModel> packageModelMap = packageUseCase.getPackageByIds(packageIds);
        CustomerModel customerModel = customerUseCase.getCustomerById(transactionRequest.getCustomerId());
        UserModel userModel = userUseCase.getDomainUserById(transactionRequest.getUserId());
        BigDecimal totalAllPackagesPrice = BigDecimal.ZERO;

        List<TransactionDetailModel> transactionDetailModels = new ArrayList<>();
        LocalDateTime maxEstimationDate = LocalDateTime.now();

        for (TransactionDetailRequestDTO detailRequestDTO : transactionRequest.getTransactionDetails()) {
            PackageModel packageModel = packageModelMap.get(detailRequestDTO.getPackageId());
            Integer quantity = detailRequestDTO.getQuantity();
            BigDecimal totalPackagePrice = new BigDecimal(quantity * packageModel.getPrice().intValue());

            TransactionDetailModel transactionDetailModel = new TransactionDetailModel();
            transactionDetailModel.setPkg(packageModel);
            transactionDetailModel.setPackageName(packageModel.getName());
            transactionDetailModel.setPackageDescription(packageModel.getDescription());
            transactionDetailModel.setPackageType(packageModel.getType());
            transactionDetailModel.setPackageDaysEstimation(packageModel.getDaysEstimation());

            transactionDetailModel.setQuantity(quantity);
            transactionDetailModel.setTotalPrice(totalPackagePrice);
            transactionDetailModel.setTargetDate(detailRequestDTO.getOrderDate().plusDays(packageModel.getDaysEstimation()));

            transactionDetailModels.add(transactionDetailModel);
            totalAllPackagesPrice = totalAllPackagesPrice.add(totalPackagePrice);

            if (transactionDetailModel.getTargetDate().isAfter(maxEstimationDate))
                maxEstimationDate = transactionDetailModel.getTargetDate();
        }

        MultipartFile paymentReceiptFile = transactionRequest.getPaymentReceiptFile();
        MultipartFile invoiceDocumentFile = transactionRequest.getInvoiceDocumentFile();
        List<MultipartFile> conditionBeforeFiles = transactionRequest.getConditionBeforeFiles();
        List<MultipartFile> conditionAfterFiles = transactionRequest.getConditionAfterFiles();
        List<MultipartFile> otherFiles = transactionRequest.getOtherFiles();

        List<TransactionFileModel> transactionFileModels = transactionFileUseCase.setTransactionFiles(
                invoiceNo, paymentReceiptFile,
                invoiceDocumentFile, conditionBeforeFiles,
                conditionAfterFiles, otherFiles);

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setOrderDate(transactionRequest.getOrderDate());
        transactionModel.setStatus(TransactionStatus.IN_PROGRESS);
        transactionModel.setTargetDate(ValueUtil.orElseLocalDateTime(transactionRequest.getTargetDate(), maxEstimationDate));
        transactionModel.setCustomerName(customerModel.getName());
        transactionModel.setCustomerPhone(customerModel.getPhone());
        transactionModel.setCustomerAddress(customerModel.getAddress());
        transactionModel.setCustomer(customerModel);
        transactionModel.setUser(userModel);
        transactionModel.setInvoiceNo(invoiceNo);
        transactionModel.setTotalPrice(totalAllPackagesPrice);
        transactionModel.setTransactionDetails(transactionDetailModels);
        transactionModel.setTransactionFiles(transactionFileModels);

        return TransactionMapper.mapDomainToResponse(
                transactionRepositoryPort.save(transactionModel));
    }

    @Override
    public TransactionResponseDTO getTransactionById(String id) {
        return TransactionMapper.mapDomainToResponse(transactionRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Transaksi tidak ditemukan")));
    }

    @Override
    public Page<TransactionResponseDTO> getAllTransactions(TransactionFilter transactionFilter, Pageable pageable) {
        return transactionRepositoryPort.findAll(transactionFilter, pageable)
                .map(TransactionMapper::mapDomainToResponse);
    }

    // UPDATE DISINI TIDAK ADA UPDATE FILE, UPDATE FILE ADA ENDPOINT TERSENDIRI
    @Override
    @Transactional
    public TransactionResponseDTO updateTransaction(String id, TransactionRequestDTO transactionRequest) {
        TransactionModel existingTransactionModel = transactionRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Transaksi tidak ditemukan"));

        List<String> packageIds = transactionRequest.getTransactionDetails().stream().map(TransactionDetailRequestDTO::getPackageId).toList();
        Map<String, PackageModel> packageModelMap = packageUseCase.getPackageByIds(packageIds);
        Map<String, TransactionDetailModel> detailModelMap = transactionDetailUseCase.getByTransactionIdMap(existingTransactionModel.getId());

        BigDecimal totalAllPackagesPrice = BigDecimal.ZERO;

        List<TransactionDetailModel> transactionDetailModels = new ArrayList<>();

        LocalDateTime maxEstimationDate = LocalDateTime.now();

        for (TransactionDetailRequestDTO detailRequest : transactionRequest.getTransactionDetails()) {
            PackageModel packageModel = packageModelMap.get(detailRequest.getPackageId());
            Integer quantity = detailRequest.getQuantity();
            BigDecimal totalPackagePrice = new BigDecimal(quantity * packageModel.getPrice().intValue());

            TransactionDetailModel transactionDetailModel;
            if (detailModelMap.containsKey(detailRequest.getId())) {
                transactionDetailModel = detailModelMap.get(detailRequest.getId());
            } else {
                transactionDetailModel = new TransactionDetailModel();
                transactionDetailModel.setTargetDate(detailRequest.getOrderDate().plusDays(packageModel.getDaysEstimation()));
            }

            transactionDetailModel.setPkg(packageModel);
            transactionDetailModel.setPackageName(packageModel.getName());
            transactionDetailModel.setPackageDescription(packageModel.getDescription());
            transactionDetailModel.setPackageType(packageModel.getType());
            transactionDetailModel.setPackageDaysEstimation(packageModel.getDaysEstimation());

            transactionDetailModel.setQuantity(quantity);
            transactionDetailModel.setTotalPrice(totalPackagePrice);

            transactionDetailModels.add(transactionDetailModel);

            totalAllPackagesPrice = totalAllPackagesPrice.add(totalPackagePrice);

            if (transactionDetailModel.getTargetDate().isAfter(maxEstimationDate))
                maxEstimationDate = transactionDetailModel.getTargetDate();
        }

        existingTransactionModel.setOrderDate(ValueUtil.orElseLocalDateTime(transactionRequest.getOrderDate(), existingTransactionModel.getOrderDate()));
        existingTransactionModel.setTargetDate(maxEstimationDate);
        existingTransactionModel.setStatus(transactionRequest.getStatus());
        existingTransactionModel.setTotalPrice(totalAllPackagesPrice);
        existingTransactionModel.setTransactionDetails(transactionDetailModels);

        return TransactionMapper.mapDomainToResponse(
                transactionRepositoryPort.save(existingTransactionModel));
    }

    @Override
    public void deleteTransaction(String id) {
        TransactionResponseDTO transaction =  this.getTransactionById(id);

        if (ValueUtil.hasValueList(transaction.getTransactionFiles())) {
            TransactionFileDeleteRequestDTO transactionFileDeleteRequest = new TransactionFileDeleteRequestDTO();
            transactionFileDeleteRequest.setTransactionFileIds(
                    transaction.getTransactionFiles().stream().map(TransactionFileModel::getId).toList());
            transactionFileUseCase.deleteListFile(transactionFileDeleteRequest);
        }

        transactionRepositoryPort.deleteById(id);
    }


}
