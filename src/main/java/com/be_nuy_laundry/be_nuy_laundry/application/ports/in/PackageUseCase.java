package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import java.util.List;
import java.util.Map;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg.PackageRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PackageUseCase {
    PackageModel createPackage(PackageRequestDTO request);
    PackageModel getPackageById(String id);
    List<PackageModel> getPackageByBenefitId(String benefitId);
    Map<String, PackageModel> getPackageByIds(List<String> ids);
    Page<PackageModel> getAllPackages(PackageFilter packageFilter, Pageable pageable);
    PackageModel updatePackage(String id, PackageRequestDTO request);
    void deletePackage(String id);
}