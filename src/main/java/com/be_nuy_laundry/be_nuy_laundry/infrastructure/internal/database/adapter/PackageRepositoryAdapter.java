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
        PackageEntity packageEntity;
        if (pkg.getId() != null) {
            packageEntity = packageJpaRepository.findById(pkg.getId())
                    .orElseGet(() -> PackageMapper.mapToEntity(pkg));
            packageEntity.setName(pkg.getName());
            packageEntity.setDescription(pkg.getDescription());
            packageEntity.setType(pkg.getType());
            packageEntity.setPrice(pkg.getPrice());
            packageEntity.setDaysEstimation(pkg.getDaysEstimation());
            packageEntity.setPhoto(pkg.getPhoto());
        } else {
            packageEntity = PackageMapper.mapToEntity(pkg);
        }

        List<String> distinctBenefitIds = (pkg.getBenefitIds() == null)
                ? List.of()
                : pkg.getBenefitIds().stream().filter(java.util.Objects::nonNull).distinct().toList();

        List<BenefitEntity> validBenefitEntities = distinctBenefitIds.isEmpty()
                ? List.of()
                : benefitJpaRepository.findAllById(distinctBenefitIds);

        if (validBenefitEntities.size() != distinctBenefitIds.size())
            throw new DataNotFoundException("Beberapa benefit ID tidak ditemukan");

        packageEntity.clearBenefits();

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

    @Override
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.packages.PackageMetricsDTO getMetrics() {
        return com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.packages.PackageMetricsDTO.builder()
                .totalPackages(packageJpaRepository.count())
                .activePackages(packageJpaRepository.count())
                .weightPackages(packageJpaRepository.countByType(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.WEIGHT))
                .piecePackages(packageJpaRepository.countByType(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType.PCS))
                .build();
    }
}

