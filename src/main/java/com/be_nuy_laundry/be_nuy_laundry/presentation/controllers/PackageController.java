package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg.PackageRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.PackageUseCase;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Validated
public class PackageController {

    private final PackageUseCase packageUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PackageModel>> create(
            @RequestPart("request") @Valid PackageRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file) {
        request.setFile(file);

        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, packageUseCase.createPackage(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageModel>> getById(@PathVariable String id) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, packageUseCase.getPackageById(id));
    }

    @GetMapping("/{benefitId}/benefit")
    public ResponseEntity<ApiResponse<List<PackageModel>>> getByBenefitId(@PathVariable String benefitId) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, packageUseCase.getPackageByBenefitId(benefitId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PackageModel>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) PackageType type) {
        PackageFilter packageFilter = PackageFilter.builder()
                .name(name)
                .description(description)
                .type(type)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG,
                packageUseCase.getAllPackages(packageFilter, pageable));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PackageModel>> update(
            @PathVariable String id,
            @RequestPart("request") @Valid PackageRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file) {
        request.setFile(file);

        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, packageUseCase.updatePackage(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        packageUseCase.deletePackage(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

}
