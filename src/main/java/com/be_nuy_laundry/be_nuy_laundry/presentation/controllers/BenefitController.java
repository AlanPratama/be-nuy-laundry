package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.BenefitUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.benefit.BenefitRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitUseCase benefitUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BenefitModel>> create(@Valid @RequestBody BenefitRequestDTO request) {
        BenefitModel pkg = request.toModel();

        BenefitModel created = benefitUseCase.createBenefit(pkg);
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BenefitModel>> getById(@PathVariable String id) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, benefitUseCase.getBenefitById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BenefitModel>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name
    ) {
        BenefitFilter benefitFilter = BenefitFilter.builder()
                .name(name)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, benefitUseCase.getAllBenefits(benefitFilter, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BenefitModel>> update(@PathVariable String id, @Valid @RequestBody BenefitRequestDTO request) {
        BenefitModel pkg = request.toModel();

        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, benefitUseCase.updateBenefit(id, pkg));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        benefitUseCase.deleteBenefit(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }


}
