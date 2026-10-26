package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;

import java.util.Map;

public interface TransactionDetailUseCase {
    Map<String, TransactionDetailModel> getByTransactionIdMap(String transactionId);
    void deleteById(String transactionDetailId);
    TransactionDetailModel markDelivered(String transactionDetailId);
}

