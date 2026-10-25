package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionUseCase transactionUseCase;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    @DisplayName("givenValidRequestAndFiles_whenCreateTransaction_thenReturnTransactionResponse")
    void givenValidRequestAndFiles_whenCreateTransaction_thenReturnTransactionResponse() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCustomerId("cust-1");

        MultipartFile receipt = new MockMultipartFile("receipt", "receipt.png", "image/png", new byte[]{1});
        MultipartFile invoice = new MockMultipartFile("invoice", "invoice.pdf", "application/pdf", new byte[]{2});
        List<MultipartFile> beforeList = List.of(new MockMultipartFile("before", "before.png", "image/png", new byte[]{3}));
        List<MultipartFile> afterList = List.of(new MockMultipartFile("after", "after.png", "image/png", new byte[]{4}));
        List<MultipartFile> otherList = List.of(new MockMultipartFile("other", "other.png", "image/png", new byte[]{5}));

        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId("trx-1");

        when(transactionUseCase.createTransaction(request)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = transactionController.create(
                request, receipt, invoice, beforeList, afterList, otherList
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        assertThat(request.getPaymentReceiptFile()).isEqualTo(receipt);
        assertThat(request.getInvoiceDocumentFile()).isEqualTo(invoice);
        assertThat(request.getConditionBeforeFiles()).isEqualTo(beforeList);
        assertThat(request.getConditionAfterFiles()).isEqualTo(afterList);
        assertThat(request.getOtherFiles()).isEqualTo(otherList);
        verify(transactionUseCase).createTransaction(request);
    }

    @Test
    @DisplayName("givenTransactionId_whenGetById_thenReturnTransactionResponse")
    void givenTransactionId_whenGetById_thenReturnTransactionResponse() {
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId("trx-1");
        when(transactionUseCase.getTransactionById("trx-1")).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = transactionController.getById("trx-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo("trx-1");
        verify(transactionUseCase).getTransactionById("trx-1");
    }

    @Test
    @DisplayName("givenFiltersAndPageable_whenGetAll_thenReturnPageResponse")
    void givenFiltersAndPageable_whenGetAll_thenReturnPageResponse() {
        Page<TransactionResponseDTO> page = new PageImpl<>(List.of(new TransactionResponseDTO()));

        when(transactionUseCase.getAllTransactions(any(TransactionFilter.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<TransactionResponseDTO>>> response =
                transactionController.getAll(0, 10, "createdAt", "desc", "Alice", com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus.IN_PROGRESS, null, null, null, null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        ArgumentCaptor<TransactionFilter> filterCaptor = ArgumentCaptor.forClass(TransactionFilter.class);
        verify(transactionUseCase).getAllTransactions(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getName()).isEqualTo("Alice");
        assertThat(filterCaptor.getValue().getStatus()).isEqualTo(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("givenPaymentRequest_whenUpdatePayment_thenReturnUpdatedResponse")
    void givenPaymentRequest_whenUpdatePayment_thenReturnUpdatedResponse() {
        com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionPaymentUpdateRequestDTO request =
                com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionPaymentUpdateRequestDTO.builder()
                        .downPayment(java.math.BigDecimal.valueOf(50000))
                        .paymentStatus(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus.PAID)
                        .build();

        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId("trx-1");

        when(transactionUseCase.updateTransactionPayment(eq("trx-1"), eq(request))).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> patchResp = transactionController.updatePayment("trx-1", request);
        ResponseEntity<ApiResponse<TransactionResponseDTO>> putResp = transactionController.updatePaymentPut("trx-1", request);

        assertThat(patchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transactionUseCase, times(2)).updateTransactionPayment("trx-1", request);
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdateTransaction_thenReturnUpdatedResponse")
    void givenValidRequest_whenUpdateTransaction_thenReturnUpdatedResponse() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCustomerId("cust-2");

        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId("trx-1");

        when(transactionUseCase.updateTransaction(eq("trx-1"), eq(request))).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> response = transactionController.update("trx-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        verify(transactionUseCase).updateTransaction("trx-1", request);
    }

    @Test
    @DisplayName("givenStatusRequest_whenUpdateStatus_thenReturnUpdatedResponse")
    void givenStatusRequest_whenUpdateStatus_thenReturnUpdatedResponse() {
        com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionStatusUpdateRequestDTO request =
                new com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionStatusUpdateRequestDTO(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus.DONE);

        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId("trx-1");

        when(transactionUseCase.updateTransactionStatus(eq("trx-1"), eq(request))).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<TransactionResponseDTO>> patchResp = transactionController.updateStatus("trx-1", request);
        ResponseEntity<ApiResponse<TransactionResponseDTO>> putResp = transactionController.updateStatusPut("trx-1", request);

        assertThat(patchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transactionUseCase, times(2)).updateTransactionStatus("trx-1", request);
    }

    @Test
    @DisplayName("givenTransactionId_whenDeleteTransaction_thenReturnSuccessResponse")
    void givenTransactionId_whenDeleteTransaction_thenReturnSuccessResponse() {
        doNothing().when(transactionUseCase).deleteTransaction("trx-1");

        ResponseEntity<ApiResponse<Void>> response = transactionController.delete("trx-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(transactionUseCase).deleteTransaction("trx-1");
    }
}
