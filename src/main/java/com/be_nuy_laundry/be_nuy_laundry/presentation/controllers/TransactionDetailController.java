package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionDetailUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transaction-details")
@RequiredArgsConstructor
@Validated
public class TransactionDetailController {

    private final TransactionDetailUseCase transactionDetailUseCase;

    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        transactionDetailUseCase.deleteById(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

    @PatchMapping("/{id}/delivered")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel>> markDelivered(@PathVariable String id) {
        com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel result = transactionDetailUseCase.markDelivered(id);
        return ResponseUtil.success("SUCCESS", result);
    }

}
