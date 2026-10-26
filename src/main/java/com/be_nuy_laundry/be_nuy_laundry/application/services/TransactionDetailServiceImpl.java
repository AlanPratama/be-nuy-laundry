package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionDetailUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionDetailRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionDetailServiceImpl implements TransactionDetailUseCase {

    private final TransactionDetailRepositoryPort transactionDetailRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final com.be_nuy_laundry.be_nuy_laundry.application.ports.in.CustomerUseCase customerUseCase;

    @Override
    public Map<String, TransactionDetailModel> getByTransactionIdMap(String transactionId) {
        List<TransactionDetailModel> transactionModels = transactionDetailRepositoryPort.findByTransactionId(transactionId);

        Map<String, TransactionDetailModel> result = new HashMap<>();
        for (TransactionDetailModel transactionModel : transactionModels) {
            result.putIfAbsent(transactionModel.getId(), transactionModel);
        }

        return result;
    }

    public TransactionDetailModel findById(String transactionDetailId) {
        return transactionDetailRepositoryPort.findById(transactionDetailId)
                .orElseThrow(() -> new DataNotFoundException("Transaksi detail tidak ditemukan"));
    }

    @Override
    @Transactional
    public TransactionDetailModel markDelivered(String transactionDetailId) {
        TransactionDetailModel trxDetailModel = this.findById(transactionDetailId);
        trxDetailModel.setDeliveredDate(java.time.LocalDateTime.now());
        TransactionDetailModel updated = transactionDetailRepositoryPort.save(trxDetailModel);

        if (trxDetailModel.getTransactionId() != null) {
            TransactionModel existingTransactionModel = transactionRepositoryPort.findById(trxDetailModel.getTransactionId()).orElse(null);
            if (existingTransactionModel != null) {
                List<TransactionDetailModel> allDetails = transactionDetailRepositoryPort.findByTransactionId(existingTransactionModel.getId());
                boolean allDelivered = allDetails.stream().allMatch(d -> d.getDeliveredDate() != null);
                if (allDelivered && existingTransactionModel.getStatus() != com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus.DONE) {
                    existingTransactionModel.setStatus(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus.DONE);
                    
                    if (existingTransactionModel.getCustomer() != null && existingTransactionModel.getCustomer().getId() != null) {
                        try {
                            com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel cust = customerUseCase.getCustomerById(existingTransactionModel.getCustomer().getId());
                            java.math.BigDecimal totalKg = java.math.BigDecimal.ZERO;
                            int totalPcs = 0;
                            for (TransactionDetailModel d : allDetails) {
                                if (d.getPackageType() == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.WEIGHT) {
                                    totalKg = totalKg.add(java.math.BigDecimal.valueOf(d.getQuantity() != null ? d.getQuantity() : 0));
                                } else if (d.getPackageType() == com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.PCS) {
                                    totalPcs += (d.getQuantity() != null ? d.getQuantity() : 0);
                                }
                            }
                            java.math.BigDecimal currentKg = cust.getTotalCucianKg() != null ? cust.getTotalCucianKg() : java.math.BigDecimal.ZERO;
                            int currentPcs = cust.getTotalCucianSatuan() != null ? cust.getTotalCucianSatuan() : 0;
                            cust.setTotalCucianKg(currentKg.add(totalKg));
                            cust.setTotalCucianSatuan(currentPcs + totalPcs);
                            customerUseCase.updateCustomer(cust.getId(), cust);
                        } catch (Exception ignored) {
                        }
                    }
                    transactionRepositoryPort.save(existingTransactionModel);
                }
            }
        }

        return updated;
    }

    @Override
    @Transactional
    public void deleteById(String transactionDetailId) {
        TransactionDetailModel trxDetailModel = this.findById(transactionDetailId);
        TransactionModel existingTransactionModel =
                transactionRepositoryPort.findById(trxDetailModel.getTransactionId())
                        .orElseThrow(() -> new DataNotFoundException("Transaksi tidak ditemukan"));

        if (existingTransactionModel.getTransactionDetails().size() <= 1)
            throw new ConflictException("Minimal harus ada 1 transaksi detail");

        transactionDetailRepositoryPort.deleteById(transactionDetailId);

        existingTransactionModel.setTotalPrice(
                existingTransactionModel.getTotalPrice().subtract(trxDetailModel.getTotalPrice()));

        existingTransactionModel.setTransactionDetails(
                existingTransactionModel.getTransactionDetails()
                        .stream()
                        .filter((data)
                                -> !data.getId().equals(transactionDetailId))
                        .toList());

        transactionRepositoryPort.save(existingTransactionModel);
    }
}
