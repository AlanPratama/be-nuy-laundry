package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionFileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileUploadRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file.TransactionFileDeleteResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction-files")
@RequiredArgsConstructor
public class TransactionFileController {

    private final TransactionFileUseCase transactionFileUseCase;

    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<TransactionFileModel>>> upload(
            @RequestPart("request") @Valid TransactionFileUploadRequestDTO request,
            @RequestPart("paymentReceiptFile") @ValidImage MultipartFile paymentReceiptFile,
            @RequestPart("invoiceDocumentFile") @ValidImage MultipartFile invoiceDocumentFile,
            @RequestPart("conditionBeforeFiles") @ValidImage List<MultipartFile> conditionBeforeFiles,
            @RequestPart("conditionAfterFiles") @ValidImage List<MultipartFile> conditionAfterFiles,
            @RequestPart("otherFiles") @ValidImage List<MultipartFile> otherFiles
    ) {
        request.setPaymentReceiptFile(paymentReceiptFile);
        request.setInvoiceDocumentFile(invoiceDocumentFile);
        request.setConditionBeforeFiles(conditionBeforeFiles);
        request.setConditionAfterFiles(conditionAfterFiles);
        request.setOtherFiles(otherFiles);

        List<TransactionFileModel> created = transactionFileUseCase.uploadTransactionFile(request);
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        transactionFileUseCase.deleteTransactionFile(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

    @DeleteMapping("/list")
    public ResponseEntity<ApiResponse<List<TransactionFileDeleteResponseDTO>>> deleteList(@RequestBody TransactionFileDeleteRequestDTO request) {
        List<TransactionFileDeleteResponseDTO> transactionFileDeleteResponses = transactionFileUseCase.deleteListTransactionFile(request);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, transactionFileDeleteResponses);
    }

}
