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
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionStatusUpdateRequestDTO;
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
    private final FileUseCase fileUseCase;

    @Override
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
            transactionDetailModel.setPackageId(packageModel.getId());
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

        // FASE 1: I/O Jaringan ke MinIO (Di luar transaksi DB)
        List<TransactionFileModel> transactionFileModels = transactionFileUseCase.setTransactionFiles(
                invoiceNo, paymentReceiptFile,
                invoiceDocumentFile, conditionBeforeFiles,
                conditionAfterFiles, otherFiles);

        BigDecimal downPayment = transactionRequest.getDownPayment() != null ? transactionRequest.getDownPayment() : BigDecimal.ZERO;
        com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus paymentStatus = transactionRequest.getPaymentStatus();
        if (paymentStatus == null) {
            if (downPayment.compareTo(BigDecimal.ZERO) <= 0) {
                paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.UNPAID;
                downPayment = BigDecimal.ZERO;
            } else if (downPayment.compareTo(totalAllPackagesPrice) >= 0) {
                paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID;
            } else {
                paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.DOWN_PAYMENT;
            }
        } else if (paymentStatus == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID && downPayment.compareTo(totalAllPackagesPrice) < 0) {
            downPayment = totalAllPackagesPrice;
        }

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setOrderDate(transactionRequest.getOrderDate());
        transactionModel.setStatus(TransactionStatus.IN_PROGRESS);
        transactionModel.setDownPayment(downPayment);
        transactionModel.setPaymentStatus(paymentStatus);
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

        // FASE 2: Transaksi DB singkat dengan penanganan kompensasi jika gagal
        try {
            TransactionModel saved = transactionRepositoryPort.save(transactionModel);
            try {
                int currentTotal = customerModel.getTotalTransaksi() != null ? customerModel.getTotalTransaksi() : 0;
                customerModel.setTotalTransaksi(currentTotal + 1);
                customerModel.setLastOrder(transactionRequest.getOrderDate() != null ? transactionRequest.getOrderDate() : LocalDateTime.now());
                customerUseCase.updateCustomer(customerModel.getId(), customerModel);
            } catch (Exception ignored) {
            }
            return TransactionMapper.mapDomainToResponse(saved);
        } catch (Exception e) {
            cleanupFiles(transactionFileModels);
            throw e;
        }
    }

    private void cleanupFiles(List<TransactionFileModel> files) {
        if (files == null || files.isEmpty()) return;
        List<String> filePaths = files.stream()
                .map(TransactionFileModel::getFilePath)
                .filter(ValueUtil::hasValueString)
                .toList();
        if (!filePaths.isEmpty()) {
            try {
                fileUseCase.deleteObjects(filePaths);
            } catch (Exception ignored) {
            }
        }
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

        List<String> packageIds = transactionRequest.getTransactionDetails().stream()
                .map(TransactionDetailRequestDTO::getPackageId)
                .filter(ValueUtil::hasValueString)
                .toList();
        Map<String, PackageModel> packageModelMap = packageUseCase.getPackageByIds(packageIds);
        Map<String, TransactionDetailModel> detailModelMap = transactionDetailUseCase.getByTransactionIdMap(existingTransactionModel.getId());

        BigDecimal totalAllPackagesPrice = BigDecimal.ZERO;

        List<TransactionDetailModel> transactionDetailModels = new ArrayList<>();

        LocalDateTime maxEstimationDate = LocalDateTime.now();

        for (TransactionDetailRequestDTO detailRequest : transactionRequest.getTransactionDetails()) {
            PackageModel packageModel = packageModelMap.get(detailRequest.getPackageId());

            TransactionDetailModel transactionDetailModel;
            if (detailModelMap.containsKey(detailRequest.getId())) {
                transactionDetailModel = detailModelMap.get(detailRequest.getId());
                if (packageModel == null && transactionDetailModel.getPkg() != null) {
                    packageModel = transactionDetailModel.getPkg();
                }
            } else {
                if (packageModel == null) {
                    throw new DataNotFoundException("Paket tidak ditemukan");
                }
                transactionDetailModel = new TransactionDetailModel();
                transactionDetailModel.setTargetDate(detailRequest.getOrderDate().plusDays(packageModel.getDaysEstimation()));
            }

            Integer quantity = detailRequest.getQuantity() != null ? detailRequest.getQuantity() : (transactionDetailModel.getQuantity() != null ? transactionDetailModel.getQuantity() : 1);

            BigDecimal unitPrice = packageModel != null 
                    ? packageModel.getPrice() 
                    : (transactionDetailModel.getTotalPrice() != null && transactionDetailModel.getQuantity() != null && transactionDetailModel.getQuantity() > 0 
                        ? transactionDetailModel.getTotalPrice().divide(BigDecimal.valueOf(transactionDetailModel.getQuantity()), java.math.RoundingMode.HALF_UP) 
                        : BigDecimal.valueOf(8000));
            BigDecimal totalPackagePrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            if (packageModel != null) {
                transactionDetailModel.setPackageId(packageModel.getId());
                transactionDetailModel.setPkg(packageModel);
                transactionDetailModel.setPackageName(packageModel.getName());
                transactionDetailModel.setPackageDescription(packageModel.getDescription());
                transactionDetailModel.setPackageType(packageModel.getType());
                transactionDetailModel.setPackageDaysEstimation(packageModel.getDaysEstimation());
            } else if (detailRequest.getPackageId() != null) {
                transactionDetailModel.setPackageId(detailRequest.getPackageId());
            }

            transactionDetailModel.setQuantity(quantity);
            transactionDetailModel.setTotalPrice(totalPackagePrice);

            transactionDetailModels.add(transactionDetailModel);

            totalAllPackagesPrice = totalAllPackagesPrice.add(totalPackagePrice);

            if (transactionDetailModel.getTargetDate() != null && transactionDetailModel.getTargetDate().isAfter(maxEstimationDate))
                maxEstimationDate = transactionDetailModel.getTargetDate();
        }

        if (transactionRequest.getDownPayment() != null) {
            BigDecimal downPayment = transactionRequest.getDownPayment();
            com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus paymentStatus = transactionRequest.getPaymentStatus();
            if (paymentStatus == null) {
                if (downPayment.compareTo(BigDecimal.ZERO) <= 0) {
                    paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.UNPAID;
                    downPayment = BigDecimal.ZERO;
                } else if (downPayment.compareTo(totalAllPackagesPrice) >= 0) {
                    paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID;
                } else {
                    paymentStatus = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.DOWN_PAYMENT;
                }
            } else if (paymentStatus == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID && downPayment.compareTo(totalAllPackagesPrice) < 0) {
                downPayment = totalAllPackagesPrice;
            }
            existingTransactionModel.setDownPayment(downPayment);
            existingTransactionModel.setPaymentStatus(paymentStatus);
        } else if (transactionRequest.getPaymentStatus() != null) {
            com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus paymentStatus = transactionRequest.getPaymentStatus();
            existingTransactionModel.setPaymentStatus(paymentStatus);
            if (paymentStatus == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID) {
                existingTransactionModel.setDownPayment(totalAllPackagesPrice);
            }
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
    @Transactional
    public TransactionResponseDTO updateTransactionPayment(String id, com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionPaymentUpdateRequestDTO request) {
        TransactionModel existingTransactionModel = transactionRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Transaksi tidak ditemukan"));

        BigDecimal total = existingTransactionModel.getTotalPrice() != null ? existingTransactionModel.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal paid = request.getDownPayment();
        com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus status = request.getPaymentStatus();

        if (status == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID) {
            paid = total;
        } else if (paid != null) {
            if (paid.compareTo(BigDecimal.ZERO) <= 0) {
                status = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.UNPAID;
                paid = BigDecimal.ZERO;
            } else if (paid.compareTo(total) >= 0) {
                status = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID;
            } else {
                status = com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.DOWN_PAYMENT;
            }
        } else if (status != null) {
            if (status == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.UNPAID) {
                paid = BigDecimal.ZERO;
            }
        }

        if (paid != null) {
            existingTransactionModel.setDownPayment(paid);
        }
        if (status != null) {
            existingTransactionModel.setPaymentStatus(status);
        }

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

    @Override
    @Transactional
    public TransactionResponseDTO updateTransactionStatus(String id, TransactionStatusUpdateRequestDTO request) {
        TransactionModel existingTransactionModel = transactionRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Transaksi tidak ditemukan"));

        TransactionStatus oldStatus = existingTransactionModel.getStatus();
        TransactionStatus newStatus = request.getStatus();

        if (oldStatus != newStatus) {
            existingTransactionModel.setStatus(newStatus);
            if (existingTransactionModel.getCustomer() != null && existingTransactionModel.getCustomer().getId() != null) {
                try {
                    CustomerModel cust = customerUseCase.getCustomerById(existingTransactionModel.getCustomer().getId());
                    BigDecimal totalKg = BigDecimal.ZERO;
                    int totalPcs = 0;
                    if (existingTransactionModel.getTransactionDetails() != null) {
                        for (TransactionDetailModel d : existingTransactionModel.getTransactionDetails()) {
                            if (d.getPackageType() == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.WEIGHT) {
                                totalKg = totalKg.add(BigDecimal.valueOf(d.getQuantity() != null ? d.getQuantity() : 0));
                            } else if (d.getPackageType() == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.PCS) {
                                totalPcs += (d.getQuantity() != null ? d.getQuantity() : 0);
                            }
                        }
                    }

                    BigDecimal currentKg = cust.getTotalCucianKg() != null ? cust.getTotalCucianKg() : BigDecimal.ZERO;
                    int currentPcs = cust.getTotalCucianSatuan() != null ? cust.getTotalCucianSatuan() : 0;

                    if (newStatus == TransactionStatus.DONE && oldStatus != TransactionStatus.DONE) {
                        cust.setTotalCucianKg(currentKg.add(totalKg));
                        cust.setTotalCucianSatuan(currentPcs + totalPcs);
                        customerUseCase.updateCustomer(cust.getId(), cust);
                    } else if (oldStatus == TransactionStatus.DONE && newStatus != TransactionStatus.DONE) {
                        cust.setTotalCucianKg(currentKg.subtract(totalKg).max(BigDecimal.ZERO));
                        cust.setTotalCucianSatuan(Math.max(0, currentPcs - totalPcs));
                        customerUseCase.updateCustomer(cust.getId(), cust);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return TransactionMapper.mapDomainToResponse(
                transactionRepositoryPort.save(existingTransactionModel));
    }


    @Override
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionMetricsDTO getMetrics() {
        return transactionRepositoryPort.getMetrics();
    }

}
