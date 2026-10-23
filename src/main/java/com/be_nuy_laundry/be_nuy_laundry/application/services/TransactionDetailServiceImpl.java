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
