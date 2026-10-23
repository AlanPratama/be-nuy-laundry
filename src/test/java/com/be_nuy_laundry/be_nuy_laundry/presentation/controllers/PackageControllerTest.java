package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.PackageUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg.PackageRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageControllerTest {

    @Mock
    private PackageUseCase packageUseCase;

    @InjectMocks
    private PackageController packageController;

    @Test
    @DisplayName("givenValidRequest_whenCreatePackage_thenReturnPackageModel")
    void givenValidRequest_whenCreatePackage_thenReturnPackageModel() {
        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Cuci Komplit");
        request.setPrice(BigDecimal.valueOf(10000));
        request.setType(PackageType.WEIGHT);
        MultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2});

        PackageModel createdModel = TestDataBuilder.createSamplePackage();

        when(packageUseCase.createPackage(request)).thenReturn(createdModel);

        ResponseEntity<ApiResponse<PackageModel>> response = packageController.create(request, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(createdModel);
        assertThat(request.getFile()).isEqualTo(file);
        verify(packageUseCase).createPackage(request);
    }

    @Test
    @DisplayName("givenPackageId_whenGetById_thenReturnPackageModel")
    void givenPackageId_whenGetById_thenReturnPackageModel() {
        PackageModel packageModel = TestDataBuilder.createSamplePackage();
        when(packageUseCase.getPackageById("pkg-1")).thenReturn(packageModel);

        ResponseEntity<ApiResponse<PackageModel>> response = packageController.getById("pkg-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(packageModel.getId());
        verify(packageUseCase).getPackageById("pkg-1");
    }

    @Test
    @DisplayName("givenBenefitId_whenGetByBenefitId_thenReturnPackageModelList")
    void givenBenefitId_whenGetByBenefitId_thenReturnPackageModelList() {
        List<PackageModel> list = List.of(TestDataBuilder.createSamplePackage());
        when(packageUseCase.getPackageByBenefitId("ben-1")).thenReturn(list);

        ResponseEntity<ApiResponse<List<PackageModel>>> response = packageController.getByBenefitId("ben-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        verify(packageUseCase).getPackageByBenefitId("ben-1");
    }

    @Test
    @DisplayName("givenFiltersAndPageable_whenGetAll_thenReturnPageResponse")
    void givenFiltersAndPageable_whenGetAll_thenReturnPageResponse() {
        Page<PackageModel> page = new PageImpl<>(List.of(TestDataBuilder.createSamplePackage()));

        when(packageUseCase.getAllPackages(any(PackageFilter.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<PackageModel>>> response =
                packageController.getAll(0, 10, "name", "asc", "Cuci", "Bersih", PackageType.WEIGHT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        ArgumentCaptor<PackageFilter> filterCaptor = ArgumentCaptor.forClass(PackageFilter.class);
        verify(packageUseCase).getAllPackages(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getName()).isEqualTo("Cuci");
        assertThat(filterCaptor.getValue().getDescription()).isEqualTo("Bersih");
        assertThat(filterCaptor.getValue().getType()).isEqualTo(PackageType.WEIGHT);
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdatePackage_thenReturnUpdatedPackage")
    void givenValidRequest_whenUpdatePackage_thenReturnUpdatedPackage() {
        PackageRequestDTO request = new PackageRequestDTO();
        request.setName("Cuci Komplit VIP");
        MultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2});

        PackageModel updatedModel = TestDataBuilder.createSamplePackage();
        updatedModel.setName("Cuci Komplit VIP");
        when(packageUseCase.updatePackage(eq("pkg-1"), eq(request))).thenReturn(updatedModel);

        ResponseEntity<ApiResponse<PackageModel>> response = packageController.update("pkg-1", request, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Cuci Komplit VIP");
        assertThat(request.getFile()).isEqualTo(file);
        verify(packageUseCase).updatePackage("pkg-1", request);
    }

    @Test
    @DisplayName("givenPackageId_whenDeletePackage_thenReturnSuccessResponse")
    void givenPackageId_whenDeletePackage_thenReturnSuccessResponse() {
        doNothing().when(packageUseCase).deletePackage("pkg-1");

        ResponseEntity<ApiResponse<Void>> response = packageController.delete("pkg-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(packageUseCase).deletePackage("pkg-1");
    }
}
