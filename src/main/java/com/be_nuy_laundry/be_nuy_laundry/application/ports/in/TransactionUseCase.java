package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TransactionUseCase {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest);
    TransactionResponseDTO getTransactionById(String id);
    Page<TransactionResponseDTO> getAllTransactions(TransactionFilter transactionFilter, Pageable pageable);
    TransactionResponseDTO updateTransaction(String id, TransactionRequestDTO transactionRequest);
    void deleteTransaction(String id);
}
