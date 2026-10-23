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

        return repositoryPort.save(pkg);
    }
    
    @Override
    public PackageModel getPackageById(String id) {
        return repositoryPort.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Paket tidak ditemukan"));
    }

    @Override
    public List<PackageModel> getPackageByBenefitId(String benefitId) {
        return repositoryPort.findByBenefitId(benefitId);
    }

    @Override
    public Map<String, PackageModel> getPackageByIds(List<String> ids) {

        List<String> distinctIds = ids.stream().distinct().toList();
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

        if (ValueUtil.hasValueMultipartFile(file)) {
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

        return repositoryPort.save(existing);
    }

    @Override
    @Transactional
    public void deletePackage(String id) {
        PackageModel existing = this.getPackageById(id);

        if (ValueUtil.hasValueString(existing.getPhoto()))
            fileUseCase.delete(existing.getPhoto());

        repositoryPort.deleteById(id);
    }

}
