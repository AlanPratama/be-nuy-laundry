package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.BenefitUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.BenefitRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.CacheKey;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.RedisCacheService;
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
    private final RedisCacheService redisCacheService;

    @Override
    @Transactional
    public BenefitModel createBenefit(BenefitModel benefit) {
        Optional<BenefitModel> existingEntity = benefitRepositoryPort.findByName(benefit.getName());

        if (existingEntity.isPresent())
            throw new ConflictException("Benefit sudah tersedia");

        BenefitModel saved = benefitRepositoryPort.save(benefit);
        evictCache(saved.getId());
        return saved;
    }

    @Override
    public BenefitModel getBenefitById(String id) {
        Optional<BenefitModel> cached = redisCacheService.get(CacheKey.BENEFIT_DETAIL + id, BenefitModel.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        BenefitModel model = benefitRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Benefit tidak ditemukan"));
        redisCacheService.set(CacheKey.BENEFIT_DETAIL + id, model, 3600);
        return model;
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

        BenefitModel saved = benefitRepositoryPort.save(existingModel);
        evictCache(id);
        return saved;
    }

    @Override
    @Transactional
    public void deleteBenefit(String id) {
        this.getBenefitById(id);
        benefitRepositoryPort.deleteById(id);
        evictCache(id);
    }

    private void evictCache(String id) {
        if (ValueUtil.hasValueString(id)) {
            redisCacheService.delete(CacheKey.BENEFIT_DETAIL + id);
        }
        redisCacheService.deleteByPattern(CacheKey.BENEFIT_PATTERN);
    }
}
