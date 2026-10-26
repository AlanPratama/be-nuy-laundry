package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;

import java.util.List;
import java.util.Optional;

public interface TransactionDetailRepositoryPort {
    List<TransactionDetailModel> findByTransactionId(String transactionId);
    Optional<TransactionDetailModel> findById(String id);
    TransactionDetailModel save(TransactionDetailModel model);
    void deleteById(String id);
}
