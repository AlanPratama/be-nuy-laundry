package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BenefitUseCase {
    BenefitModel createBenefit(BenefitModel benefit);
    BenefitModel getBenefitById(String id);
    Page<BenefitModel> getAllBenefits(BenefitFilter benefitFilter, Pageable pageable);
    BenefitModel updateBenefit(String id, BenefitModel benefit);
    void deleteBenefit(String id);
}
