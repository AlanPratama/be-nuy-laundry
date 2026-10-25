package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.PackageUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.PackageRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.Constant;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg.PackageRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.CacheKey;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.RedisCacheService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageUseCase{

    private final PackageRepositoryPort repositoryPort;
    private final FileUseCase fileUseCase;
    private final RedisCacheService redisCacheService;

    @Override
    @Transactional
    public PackageModel createPackage(PackageRequestDTO requestDTO) {
        PackageModel pkg = requestDTO.toModel();
        Optional<PackageModel> existingPkg = repositoryPort.findByName(pkg.getName());

        if (existingPkg.isPresent())
            throw new ConflictException("Paket sudah tersedia");

        String filePath = Constant.EMPTY_STRING;
        if (ValueUtil.hasValueMultipartFile(requestDTO.getFile())) {
            filePath = fileUseCase.upload(
                    requestDTO.getFile(),
                    requestDTO.getName(),
                    MinIoConstant.FOLDER.PACKAGE);
        }

        pkg.setPhoto(filePath);

        PackageModel saved = repositoryPort.save(pkg);
        evictCache(saved.getId());
        return saved;
    }
    
    @Override
    public PackageModel getPackageById(String id) {
        Optional<PackageModel> cached = redisCacheService.get(CacheKey.PACKAGE_DETAIL + id, PackageModel.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        PackageModel model = repositoryPort.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Paket tidak ditemukan"));
        redisCacheService.set(CacheKey.PACKAGE_DETAIL + id, model, 3600);
        return model;
    }

    @Override
    public List<PackageModel> getPackageByBenefitId(String benefitId) {
        return repositoryPort.findByBenefitId(benefitId);
    }

    @Override
    public Map<String, PackageModel> getPackageByIds(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return new HashMap<>();

        List<String> distinctIds = ids.stream()
                .filter(ValueUtil::hasValueString)
                .distinct()
                .toList();

        if (distinctIds.isEmpty())
            return new HashMap<>();

        List<PackageModel> packageModels = repositoryPort.findByIds(distinctIds);
        if (distinctIds.size() != packageModels.size())
            throw new DataNotFoundException("Beberapa paket tidak ditemukan");

        Map<String, PackageModel> packageModelMap = new HashMap<>();

        for (PackageModel packageModel : packageModels) {
            packageModelMap.putIfAbsent(packageModel.getId(), packageModel);
        }

        return packageModelMap;
    }

    @Override
    public Page<PackageModel> getAllPackages(PackageFilter packageFilter, Pageable pageable) {
        return repositoryPort.findAll(packageFilter, pageable);
    }

    @Override
    @Transactional
    public PackageModel updatePackage(String id, PackageRequestDTO requestDTO) {
        PackageModel pkg = requestDTO.toModel();
        MultipartFile file = requestDTO.getFile();
        PackageModel existing = this.getPackageById(id);
        String filePath = existing.getPhoto();

        if (Boolean.TRUE.equals(requestDTO.getRemovePhoto())) {
            if (ValueUtil.hasValueString(existing.getPhoto())) {
                fileUseCase.delete(existing.getPhoto());
            }
            filePath = null;
        } else if (ValueUtil.hasValueMultipartFile(file)) {
            if (ValueUtil.hasValueString(existing.getPhoto())) {
                fileUseCase.delete(existing.getPhoto());
            }

            filePath = fileUseCase.upload(
                    requestDTO.getFile(),
                    requestDTO.getName(),
                    MinIoConstant.FOLDER.PACKAGE);
        }

        existing.setName(pkg.getName());
        existing.setDescription(pkg.getDescription());
        existing.setType(pkg.getType());
        existing.setPrice(pkg.getPrice());
        existing.setDaysEstimation(pkg.getDaysEstimation());
        existing.setPhoto(filePath);
        existing.setBenefitIds(pkg.getBenefitIds());

        PackageModel saved = repositoryPort.save(existing);
        evictCache(id);
        return saved;
    }

    @Override
    @Transactional
    public void deletePackage(String id) {
        PackageModel existing = this.getPackageById(id);

        if (ValueUtil.hasValueString(existing.getPhoto()))
            fileUseCase.delete(existing.getPhoto());

        repositoryPort.deleteById(id);
        evictCache(id);
    }

    private void evictCache(String id) {
        if (ValueUtil.hasValueString(id)) {
            redisCacheService.delete(CacheKey.PACKAGE_DETAIL + id);
        }
        redisCacheService.deleteByPattern(CacheKey.PACKAGE_PATTERN);
    }

    @Override
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.packages.PackageMetricsDTO getMetrics() {
        return repositoryPort.getMetrics();
    }
}
