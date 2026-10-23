package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BenefitRepositoryPort {
    BenefitModel save(BenefitModel benefit);
    Optional<BenefitModel> findById(String id);
    Optional<BenefitModel> findByName(String name);
    Page<BenefitModel> findAll(BenefitFilter benefitFilter, Pageable pageable);
    void deleteById(String id);
}
