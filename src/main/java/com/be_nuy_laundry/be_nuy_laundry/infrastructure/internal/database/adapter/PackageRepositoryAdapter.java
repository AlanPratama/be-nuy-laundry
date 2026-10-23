package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import java.util.List;
import java.util.Optional;

import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.BenefitEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.PackageMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.BenefitJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.PackageSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.PackageRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.PackageEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.PackageJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PackageRepositoryAdapter implements PackageRepositoryPort {

    private final PackageJpaRepository packageJpaRepository;
    private final BenefitJpaRepository benefitJpaRepository;

    @Override
    public PackageModel save(PackageModel pkg) {
        PackageEntity packageEntity = PackageMapper.mapToEntity(pkg);

        List<BenefitEntity> validBenefitEntities = benefitJpaRepository.findAllById(pkg.getBenefitIds());

        if (validBenefitEntities.size() != pkg.getBenefitIds().size())
            throw new DataNotFoundException("Beberapa benefit ID tidak ditemukan");

        if (packageEntity.getId() != null) {
            packageEntity.clearBenefits();
        }

        for (BenefitEntity benefitEntity : validBenefitEntities) {
            packageEntity.addBenefit(benefitEntity);
        }

        PackageEntity savedPackageEntity = packageJpaRepository.save(packageEntity);

        return PackageMapper.mapToDomain(savedPackageEntity);
    }

    @Override
    public Optional<PackageModel> findById(String id) {
        return packageJpaRepository.findById(id)
                .map(PackageMapper::mapToDomain);
    }

    @Override
    public List<PackageModel> findByBenefitId(String benefitId) {
        return packageJpaRepository.findByBenefitId(benefitId).stream()
                .map(PackageMapper::mapToDomain)
                .toList();
    }

    @Override
    public List<PackageModel> findByIds(List<String> ids) {
        return packageJpaRepository.findByIds(ids).stream()
                .map(PackageMapper::mapToDomain)
                .toList();
    }


    @Override
    public Optional<PackageModel> findByName(String name) {
        return packageJpaRepository.findByName(name)
                .map(PackageMapper::mapToDomain);
    }

    @Override
    public Page<PackageModel> findAll(PackageFilter packageFilter, Pageable pageable) {
        Specification<PackageEntity> specification = PackageSpecification.getFilterSpecification(packageFilter);

        return packageJpaRepository.findAll(specification, pageable)
                .map(PackageMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        packageJpaRepository.deleteById(id);
    }

}

