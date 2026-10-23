package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.BenefitRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.BenefitEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.BenefitMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.BenefitJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.BenefitSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BenefitRepositoryAdapter implements BenefitRepositoryPort {

    private final BenefitJpaRepository benefitJpaRepository;

    @Override
    public BenefitModel save(BenefitModel benefit) {
        BenefitEntity entity = BenefitMapper.mapToEntity(benefit);

        BenefitEntity savedEntity = benefitJpaRepository.save(entity);

        return BenefitMapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<BenefitModel> findById(String id) {
        return benefitJpaRepository.findById(id).map(BenefitMapper::mapToDomain);
    }

    @Override
    public Optional<BenefitModel> findByName(String name) {
        return benefitJpaRepository.findByName(name).map(BenefitMapper::mapToDomain);
    }

    @Override
    public Page<BenefitModel> findAll(BenefitFilter benefitFilter, Pageable pageable) {
        Specification<BenefitEntity> specification = BenefitSpecification.getFilterSpecification(benefitFilter);

        return benefitJpaRepository.findAll(specification, pageable).map(BenefitMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        benefitJpaRepository.deleteById(id);
    }
}
