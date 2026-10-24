package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.PackageRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg.PackageRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.cache.RedisCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    private PackageRepositoryPort repositoryPort;

    @Mock
    private FileUseCase fileUseCase;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private PackageServiceImpl packageService;

    @Test
    @DisplayName("givenRequestWithFile_whenCreatePackage_thenUploadAndSavePackage")
    void givenRequestWithFile_whenCreatePackage_thenUploadAndSavePackage() {
        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Paket Kilat 1");
        request.setDescription("Deskripsi Kilat");
        request.setType(PackageType.WEIGHT);
        request.setPrice(BigDecimal.valueOf(10000));
        request.setDaysEstimation(1);
        request.setBenefitIds(List.of("b1", "b2"));
        request.setFile(new MockMultipartFile("file", "kilat.jpg", "image/jpeg", "imageContent".getBytes()));

        when(repositoryPort.findByName("Paket Kilat 1")).thenReturn(Optional.empty());
        when(fileUseCase.upload(any(), eq("Paket Kilat 1"), any())).thenReturn("/packages/kilat.jpg");
        when(repositoryPort.save(any(PackageModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackageModel result = packageService.createPackage(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Paket Kilat 1");
        assertThat(result.getPhoto()).isEqualTo("/packages/kilat.jpg");
    }

    @Test
    @DisplayName("givenRequestWithoutFile_whenCreatePackage_thenSavePackageWithEmptyPhoto")
    void givenRequestWithoutFile_whenCreatePackage_thenSavePackageWithEmptyPhoto() {
        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Paket Normal");
        request.setDescription("Deskripsi");
        request.setType(PackageType.PCS);
        request.setPrice(BigDecimal.valueOf(5000));
        request.setDaysEstimation(2);
        request.setBenefitIds(List.of("b1"));
        request.setFile(null);

        when(repositoryPort.findByName("Paket Normal")).thenReturn(Optional.empty());
        when(repositoryPort.save(any(PackageModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackageModel result = packageService.createPackage(request);

        assertThat(result).isNotNull();
        assertThat(result.getPhoto()).isEmpty();
        verifyNoInteractions(fileUseCase);
    }

    @Test
    @DisplayName("givenExistingName_whenCreatePackage_thenThrowConflictException")
    void givenExistingName_whenCreatePackage_thenThrowConflictException() {
        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Paket Duplikat");

        when(repositoryPort.findByName("Paket Duplikat")).thenReturn(Optional.of(TestDataBuilder.createSamplePackage()));

        assertThatThrownBy(() -> packageService.createPackage(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Paket sudah tersedia");
    }

    @Test
    @DisplayName("givenExistingId_whenGetPackageById_thenReturnPackageModel")
    void givenExistingId_whenGetPackageById_thenReturnPackageModel() {
        PackageModel pkg = TestDataBuilder.createSamplePackage();
        when(redisCacheService.get(anyString(), eq(PackageModel.class))).thenReturn(Optional.empty());
        when(repositoryPort.findById(pkg.getId())).thenReturn(Optional.of(pkg));

        PackageModel result = packageService.getPackageById(pkg.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(pkg.getId());
        verify(redisCacheService).set(anyString(), eq(pkg), eq(3600L));
    }

    @Test
    @DisplayName("givenCachedPackage_whenGetPackageById_thenReturnFromCacheWithoutDbQuery")
    void givenCachedPackage_whenGetPackageById_thenReturnFromCacheWithoutDbQuery() {
        PackageModel cachedPkg = TestDataBuilder.createSamplePackage();
        when(redisCacheService.get(anyString(), eq(PackageModel.class))).thenReturn(Optional.of(cachedPkg));

        PackageModel result = packageService.getPackageById(cachedPkg.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cachedPkg.getId());
        verifyNoInteractions(repositoryPort);
    }

    @Test
    @DisplayName("givenMissingId_whenGetPackageById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetPackageById_thenThrowDataNotFoundException() {
        when(repositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> packageService.getPackageById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Paket tidak ditemukan");
    }

    @Test
    @DisplayName("givenBenefitId_whenGetPackageByBenefitId_thenReturnList")
    void givenBenefitId_whenGetPackageByBenefitId_thenReturnList() {
        PackageModel pkg = TestDataBuilder.createSamplePackage();
        when(repositoryPort.findByBenefitId("benefit-1")).thenReturn(List.of(pkg));

        List<PackageModel> result = packageService.getPackageByBenefitId("benefit-1");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("givenMatchingIds_whenGetPackageByIds_thenReturnMapOfPackages")
    void givenMatchingIds_whenGetPackageByIds_thenReturnMapOfPackages() {
        PackageModel pkg1 = TestDataBuilder.createSamplePackage();
        pkg1.setId("id-1");
        PackageModel pkg2 = TestDataBuilder.createSamplePackage();
        pkg2.setId("id-2");

        List<String> ids = List.of("id-1", "id-2", "id-1"); // duplicate id-1
        when(repositoryPort.findByIds(List.of("id-1", "id-2"))).thenReturn(List.of(pkg1, pkg2));

        Map<String, PackageModel> map = packageService.getPackageByIds(ids);

        assertThat(map).hasSize(2).containsKeys("id-1", "id-2");
    }

    @Test
    @DisplayName("givenMismatchedIdsCount_whenGetPackageByIds_thenThrowDataNotFoundException")
    void givenMismatchedIdsCount_whenGetPackageByIds_thenThrowDataNotFoundException() {
        PackageModel pkg1 = TestDataBuilder.createSamplePackage();
        pkg1.setId("id-1");

        when(repositoryPort.findByIds(List.of("id-1", "id-2"))).thenReturn(List.of(pkg1));

        assertThatThrownBy(() -> packageService.getPackageByIds(List.of("id-1", "id-2")))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Beberapa paket tidak ditemukan");
    }

    @Test
    @DisplayName("givenFilterAndPageable_whenGetAllPackages_thenReturnPage")
    void givenFilterAndPageable_whenGetAllPackages_thenReturnPage() {
        PackageModel pkg = TestDataBuilder.createSamplePackage();
        Page<PackageModel> page = new PageImpl<>(List.of(pkg), PageRequest.of(0, 10), 1);
        PackageFilter filter = PackageFilter.builder().build();

        when(repositoryPort.findAll(filter, PageRequest.of(0, 10))).thenReturn(page);

        Page<PackageModel> result = packageService.getAllPackages(filter, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("givenUpdateRequestWithNewFile_whenUpdatePackage_thenDeleteOldAndUploadNew")
    void givenUpdateRequestWithNewFile_whenUpdatePackage_thenDeleteOldAndUploadNew() {
        PackageModel existing = TestDataBuilder.createSamplePackage();
        existing.setPhoto("/old/photo.jpg");

        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Updated Pkg");
        request.setDescription("Updated Desc");
        request.setType(PackageType.PCS);
        request.setPrice(BigDecimal.valueOf(20000));
        request.setDaysEstimation(3);
        request.setBenefitIds(List.of("b1"));
        request.setFile(new MockMultipartFile("file", "new.jpg", "image/jpeg", "content".getBytes()));

        when(repositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(fileUseCase.upload(any(), eq("Updated Pkg"), any())).thenReturn("/new/photo.jpg");
        when(repositoryPort.save(any(PackageModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PackageModel updated = packageService.updatePackage(existing.getId(), request);

        assertThat(updated.getName()).isEqualTo("Updated Pkg");
        assertThat(updated.getPhoto()).isEqualTo("/new/photo.jpg");
        verify(fileUseCase).delete("/old/photo.jpg");
    }

    @Test
    @DisplayName("givenPackageWithPhoto_whenDeletePackage_thenDeleteFileAndRepoRecord")
    void givenPackageWithPhoto_whenDeletePackage_thenDeleteFileAndRepoRecord() {
        PackageModel existing = TestDataBuilder.createSamplePackage();
        existing.setPhoto("/package/photo.jpg");

        when(repositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        packageService.deletePackage(existing.getId());

        verify(fileUseCase).delete("/package/photo.jpg");
        verify(repositoryPort).deleteById(existing.getId());
    }

    @Test
    @DisplayName("givenPackageWithoutPhoto_whenDeletePackage_thenOnlyDeleteRepoRecord")
    void givenPackageWithoutPhoto_whenDeletePackage_thenOnlyDeleteRepoRecord() {
        PackageModel existing = TestDataBuilder.createSamplePackage();
        existing.setPhoto(null);

        when(repositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        packageService.deletePackage(existing.getId());

        verify(repositoryPort).deleteById(existing.getId());
        verifyNoInteractions(fileUseCase);
    }
}
