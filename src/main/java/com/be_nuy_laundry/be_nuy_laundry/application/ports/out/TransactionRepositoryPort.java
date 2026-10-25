package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionMetricsDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionRepositoryPort {
    TransactionModel save(TransactionModel transactionModel);

    Optional<TransactionModel> findById(String id);

    Page<TransactionModel> findAll(TransactionFilter transactionFilter, Pageable pageable);

    String getInvoiceNo(String id);

    void deleteById(String id);

    TransactionMetricsDTO getMetrics();
}
