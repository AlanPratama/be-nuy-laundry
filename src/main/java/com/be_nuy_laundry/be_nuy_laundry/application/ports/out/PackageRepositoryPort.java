package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import java.util.List;
import java.util.Optional;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PackageRepositoryPort {
    PackageModel save(PackageModel pkg);
    Optional<PackageModel> findById(String id);
    Optional<PackageModel> findByName(String name);
    Page<PackageModel> findAll(PackageFilter packageFilter, Pageable pageable);
    List<PackageModel> findByBenefitId(String benefitId);
    List<PackageModel> findByIds(List<String> ids);
    void deleteById(String id);
    com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.packages.PackageMetricsDTO getMetrics();
}
