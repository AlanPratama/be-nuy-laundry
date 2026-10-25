package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionPaymentUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionStatusUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionUseCase transactionUseCase;
    private final String SUCCESS_INSERT_DATA_MSG = "SUCCESS";
    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> create(
            @Valid @RequestPart("request") TransactionRequestDTO request,
            @RequestPart(value = "paymentReceiptFile", required = false) @ValidImage(required = false) MultipartFile paymentReceiptFile,
            @RequestPart(value = "invoiceDocumentFile", required = false) @ValidImage(required = false) MultipartFile invoiceDocumentFile,
            @RequestPart(value = "conditionBeforeFiles", required = false) @ValidImage(required = false) List<MultipartFile> conditionBeforeFiles,
            @RequestPart(value = "conditionAfterFiles", required = false) @ValidImage(required = false) List<MultipartFile> conditionAfterFiles,
            @RequestPart(value = "otherFiles", required = false) @ValidImage(required = false) List<MultipartFile> otherFiles
    ) {
        request.setPaymentReceiptFile(paymentReceiptFile);
        request.setInvoiceDocumentFile(invoiceDocumentFile);
        request.setConditionBeforeFiles(conditionBeforeFiles);
        request.setConditionAfterFiles(conditionAfterFiles);
        request.setOtherFiles(otherFiles);

        return ResponseUtil.success(SUCCESS_INSERT_DATA_MSG, transactionUseCase.createTransaction(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> getById(@PathVariable String id) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, transactionUseCase.getTransactionById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String picId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String packageId
    ) {
        String effectiveUserId = ValueUtil.hasValueString(userId) ? userId : picId;
        TransactionFilter transactionFilter = TransactionFilter.builder()
                .name(name)
                .status(status)
                .paymentStatus(paymentStatus)
                .startDate(startDate)
                .endDate(endDate)
                .userId(effectiveUserId)
                .customerId(customerId)
                .packageId(packageId)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, transactionUseCase.getAllTransactions(transactionFilter, pageable));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionMetricsDTO>> getMetrics() {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, transactionUseCase.getMetrics());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> update(@PathVariable String id, @Valid @RequestBody TransactionRequestDTO request) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransaction(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody TransactionStatusUpdateRequestDTO request
    ) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransactionStatus(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> updateStatusPut(
            @PathVariable String id,
            @Valid @RequestBody TransactionStatusUpdateRequestDTO request
    ) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransactionStatus(id, request));
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> updatePayment(
            @PathVariable String id,
            @Valid @RequestBody TransactionPaymentUpdateRequestDTO request
    ) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransactionPayment(id, request));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> updatePaymentPut(
            @PathVariable String id,
            @Valid @RequestBody TransactionPaymentUpdateRequestDTO request
    ) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransactionPayment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        transactionUseCase.deleteTransaction(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

}
