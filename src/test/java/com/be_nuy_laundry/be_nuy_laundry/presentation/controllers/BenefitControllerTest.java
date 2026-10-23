package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.BenefitUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.benefit.BenefitRequestDTO;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BenefitControllerTest {

    @Mock
    private BenefitUseCase benefitUseCase;

    @InjectMocks
    private BenefitController benefitController;

    @Test
    @DisplayName("givenValidRequest_whenCreateBenefit_thenReturnBenefitModel")
    void givenValidRequest_whenCreateBenefit_thenReturnBenefitModel() {
        BenefitRequestDTO request = new BenefitRequestDTO();
        request.setName("Free Parfum");

        BenefitModel createdModel = TestDataBuilder.createSampleBenefit();
        when(benefitUseCase.createBenefit(any(BenefitModel.class))).thenReturn(createdModel);

        ResponseEntity<ApiResponse<BenefitModel>> response = benefitController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(createdModel);
        verify(benefitUseCase).createBenefit(any(BenefitModel.class));
    }

    @Test
    @DisplayName("givenBenefitId_whenGetById_thenReturnBenefitModel")
    void givenBenefitId_whenGetById_thenReturnBenefitModel() {
        BenefitModel benefitModel = TestDataBuilder.createSampleBenefit();
        when(benefitUseCase.getBenefitById("ben-1")).thenReturn(benefitModel);

        ResponseEntity<ApiResponse<BenefitModel>> response = benefitController.getById("ben-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(benefitModel.getId());
        verify(benefitUseCase).getBenefitById("ben-1");
    }

    @Test
    @DisplayName("givenFiltersAndPageable_whenGetAll_thenReturnPageResponse")
    void givenFiltersAndPageable_whenGetAll_thenReturnPageResponse() {
        Page<BenefitModel> page = new PageImpl<>(List.of(TestDataBuilder.createSampleBenefit()));

        when(benefitUseCase.getAllBenefits(any(BenefitFilter.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<BenefitModel>>> response =
                benefitController.getAll(0, 10, "name", "asc", "Parfum");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        ArgumentCaptor<BenefitFilter> filterCaptor = ArgumentCaptor.forClass(BenefitFilter.class);
        verify(benefitUseCase).getAllBenefits(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getName()).isEqualTo("Parfum");
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdateBenefit_thenReturnUpdatedBenefit")
    void givenValidRequest_whenUpdateBenefit_thenReturnUpdatedBenefit() {
        BenefitRequestDTO request = new BenefitRequestDTO();
        request.setName("Free Antiseptic");

        BenefitModel updatedModel = TestDataBuilder.createSampleBenefit();
        updatedModel.setName("Free Antiseptic");
        when(benefitUseCase.updateBenefit(eq("ben-1"), any(BenefitModel.class))).thenReturn(updatedModel);

        ResponseEntity<ApiResponse<BenefitModel>> response = benefitController.update("ben-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Free Antiseptic");
        verify(benefitUseCase).updateBenefit(eq("ben-1"), any(BenefitModel.class));
    }

    @Test
    @DisplayName("givenBenefitId_whenDeleteBenefit_thenReturnSuccessResponse")
    void givenBenefitId_whenDeleteBenefit_thenReturnSuccessResponse() {
        doNothing().when(benefitUseCase).deleteBenefit("ben-1");

        ResponseEntity<ApiResponse<Void>> response = benefitController.delete("ben-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(benefitUseCase).deleteBenefit("ben-1");
    }
}
