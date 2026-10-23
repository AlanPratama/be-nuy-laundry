package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> create(
            @RequestPart("request") @Valid TransactionRequestDTO request,
            @RequestPart(value = "paymentReceiptFile", required = false) @ValidImage(required = false) MultipartFile paymentReceiptFile,
            @RequestPart(value = "invoiceDocumentFile", required = false) @ValidImage(required = false) MultipartFile invoiceDocumentFile,
            @RequestPart(value = "conditionBeforeFiles", required = false) List<@ValidImage(required = false) MultipartFile> conditionBeforeFiles,
            @RequestPart(value = "conditionAfterFiles", required = false) List<@ValidImage(required = false) MultipartFile> conditionAfterFiles,
            @RequestPart(value = "otherFiles", required = false) List<@ValidImage(required = false) MultipartFile> otherFiles
    ) {
        request.setPaymentReceiptFile(paymentReceiptFile);
        request.setInvoiceDocumentFile(invoiceDocumentFile);
        request.setConditionBeforeFiles(conditionBeforeFiles);
        request.setConditionAfterFiles(conditionAfterFiles);
        request.setOtherFiles(otherFiles);

        TransactionResponseDTO created = transactionUseCase.createTransaction(request);
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, created);
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
            @RequestParam(required = false) String name
    ) {
        TransactionFilter transactionFilter = TransactionFilter.builder()
                .name(name)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, transactionUseCase.getAllTransactions(transactionFilter, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponseDTO>> update(@PathVariable String id, @Valid @RequestBody TransactionRequestDTO request) {
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, transactionUseCase.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        transactionUseCase.deleteTransaction(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }


}
