package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.BenefitUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.BenefitRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BenefitServiceImpl implements BenefitUseCase {

    private final BenefitRepositoryPort benefitRepositoryPort;

    @Override
    @Transactional
    public BenefitModel createBenefit(BenefitModel benefit) {
        Optional<BenefitModel> existingEntity = benefitRepositoryPort.findByName(benefit.getName());

        if (existingEntity.isPresent())
            throw new ConflictException("Benefit sudah tersedia");

        return benefitRepositoryPort.save(benefit);
    }

    @Override
    public BenefitModel getBenefitById(String id) {
        return benefitRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Benefit tidak ditemukan"));
    }

    @Override
    public Page<BenefitModel> getAllBenefits(BenefitFilter benefitFilter, Pageable pageable) {
        return benefitRepositoryPort.findAll(benefitFilter, pageable);
    }

    @Override
    @Transactional
    public BenefitModel updateBenefit(String id, BenefitModel benefit) {
        BenefitModel existingModel = this.getBenefitById(id);

        existingModel.setName(benefit.getName());

        return benefitRepositoryPort.save(existingModel);
    }

    @Override
    @Transactional
    public void deleteBenefit(String id) {
        this.getBenefitById(id);
        benefitRepositoryPort.deleteById(id);
    }
}
