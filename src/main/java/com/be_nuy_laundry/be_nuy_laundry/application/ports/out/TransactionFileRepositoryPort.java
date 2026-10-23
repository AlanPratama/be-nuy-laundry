package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;

import java.util.List;
import java.util.Optional;

public interface TransactionFileRepositoryPort {
    Optional<TransactionFileModel> findById(String id);
    List<TransactionFileModel> findByIds(List<String> ids);
    List<TransactionFileModel> saveAll(List<TransactionFileModel> transactionFileModels);
    void deleteById(String id);
    void deleteByIds(List<String> ids);
}
