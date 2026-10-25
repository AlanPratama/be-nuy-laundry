package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionPaymentUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionStatusUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionMetricsDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionUseCase {
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest);
    TransactionResponseDTO getTransactionById(String id);
    Page<TransactionResponseDTO> getAllTransactions(TransactionFilter transactionFilter, Pageable pageable);
    TransactionResponseDTO updateTransaction(String id, TransactionRequestDTO transactionRequest);
    TransactionResponseDTO updateTransactionStatus(String id, TransactionStatusUpdateRequestDTO request);
    TransactionResponseDTO updateTransactionPayment(String id, TransactionPaymentUpdateRequestDTO request);
    void deleteTransaction(String id);
    TransactionMetricsDTO getMetrics();
}
