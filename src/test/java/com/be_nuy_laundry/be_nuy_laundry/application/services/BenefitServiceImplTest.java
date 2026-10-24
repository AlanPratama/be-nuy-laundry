package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.BenefitRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BenefitServiceImplTest {

    @Mock
    private BenefitRepositoryPort benefitRepositoryPort;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private BenefitServiceImpl benefitService;

    @Test
    @DisplayName("givenNewBenefit_whenCreateBenefit_thenSaveAndReturnBenefit")
    void givenNewBenefit_whenCreateBenefit_thenSaveAndReturnBenefit() {
        BenefitModel benefit = TestDataBuilder.createSampleBenefit();
        when(benefitRepositoryPort.findByName(benefit.getName())).thenReturn(Optional.empty());
        when(benefitRepositoryPort.save(benefit)).thenReturn(benefit);

        BenefitModel result = benefitService.createBenefit(benefit);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(benefit.getName());
    }

    @Test
    @DisplayName("givenExistingName_whenCreateBenefit_thenThrowConflictException")
    void givenExistingName_whenCreateBenefit_thenThrowConflictException() {
        BenefitModel benefit = TestDataBuilder.createSampleBenefit();
        when(benefitRepositoryPort.findByName(benefit.getName())).thenReturn(Optional.of(benefit));

        assertThatThrownBy(() -> benefitService.createBenefit(benefit))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Benefit sudah tersedia");
    }

    @Test
    @DisplayName("givenExistingId_whenGetBenefitById_thenReturnBenefit")
    void givenExistingId_whenGetBenefitById_thenReturnBenefit() {
        BenefitModel benefit = TestDataBuilder.createSampleBenefit();
        when(redisCacheService.get(anyString(), eq(BenefitModel.class))).thenReturn(Optional.empty());
        when(benefitRepositoryPort.findById(benefit.getId())).thenReturn(Optional.of(benefit));

        BenefitModel result = benefitService.getBenefitById(benefit.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(benefit.getId());
        verify(redisCacheService).set(anyString(), eq(benefit), eq(3600L));
    }

    @Test
    @DisplayName("givenCachedBenefit_whenGetBenefitById_thenReturnFromCacheWithoutDbQuery")
    void givenCachedBenefit_whenGetBenefitById_thenReturnFromCacheWithoutDbQuery() {
        BenefitModel cached = TestDataBuilder.createSampleBenefit();
        when(redisCacheService.get(anyString(), eq(BenefitModel.class))).thenReturn(Optional.of(cached));

        BenefitModel result = benefitService.getBenefitById(cached.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cached.getId());
        verifyNoInteractions(benefitRepositoryPort);
    }

    @Test
    @DisplayName("givenMissingId_whenGetBenefitById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetBenefitById_thenThrowDataNotFoundException() {
        when(benefitRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> benefitService.getBenefitById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Benefit tidak ditemukan");
    }

    @Test
    @DisplayName("givenFilterAndPageable_whenGetAllBenefits_thenReturnPagedBenefit")
    void givenFilterAndPageable_whenGetAllBenefits_thenReturnPagedBenefit() {
        BenefitModel benefit = TestDataBuilder.createSampleBenefit();
        Page<BenefitModel> page = new PageImpl<>(List.of(benefit), PageRequest.of(0, 10), 1);
        BenefitFilter filter = BenefitFilter.builder().build();

        when(benefitRepositoryPort.findAll(filter, PageRequest.of(0, 10))).thenReturn(page);

        Page<BenefitModel> result = benefitService.getAllBenefits(filter, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("givenValidUpdate_whenUpdateBenefit_thenSaveAndReturnUpdatedBenefit")
    void givenValidUpdate_whenUpdateBenefit_thenSaveAndReturnUpdatedBenefit() {
        BenefitModel existing = TestDataBuilder.createSampleBenefit();
        BenefitModel updateData = new BenefitModel();
        updateData.setName("Extra Softener");

        when(benefitRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(benefitRepositoryPort.save(any(BenefitModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BenefitModel result = benefitService.updateBenefit(existing.getId(), updateData);

        assertThat(result.getName()).isEqualTo("Extra Softener");
    }

    @Test
    @DisplayName("givenExistingId_whenDeleteBenefit_thenDeleteById")
    void givenExistingId_whenDeleteBenefit_thenDeleteById() {
        BenefitModel existing = TestDataBuilder.createSampleBenefit();
        when(benefitRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        benefitService.deleteBenefit(existing.getId());

        verify(benefitRepositoryPort).deleteById(existing.getId());
    }
}
