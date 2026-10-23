package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.*;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.*;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.detail.TransactionDetailRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header.TransactionRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header.TransactionResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;
    @Mock
    private PackageUseCase packageUseCase;
    @Mock
    private CustomerUseCase customerUseCase;
    @Mock
    private UserUseCase userUseCase;
    @Mock
    private TransactionFileUseCase transactionFileUseCase;
    @Mock
    private TransactionDetailUseCase transactionDetailUseCase;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("givenValidRequest_whenCreateTransaction_thenCalculateAndSaveTransaction")
    void givenValidRequest_whenCreateTransaction_thenCalculateAndSaveTransaction() {
        PackageModel pkg = TestDataBuilder.createSamplePackage();
        CustomerModel customer = TestDataBuilder.createSampleCustomer();
        UserModel user = TestDataBuilder.createSampleUser();

        TransactionDetailRequestDTO detailDTO = new TransactionDetailRequestDTO();
        detailDTO.setPackageId(pkg.getId());
        detailDTO.setQuantity(2);
        detailDTO.setOrderDate(LocalDateTime.now().plusHours(1));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCustomerId(customer.getId());
        request.setUserId(user.getId());
        request.setOrderDate(LocalDateTime.now().plusHours(1));
        request.setStatus(TransactionStatus.IN_PROGRESS);
        request.setTransactionDetails(List.of(detailDTO));

        when(packageUseCase.getPackageByIds(List.of(pkg.getId()))).thenReturn(Map.of(pkg.getId(), pkg));
        when(customerUseCase.getCustomerById(customer.getId())).thenReturn(customer);
        when(userUseCase.getDomainUserById(user.getId())).thenReturn(user);
        when(transactionFileUseCase.setTransactionFiles(any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(transactionRepositoryPort.save(any(TransactionModel.class))).thenAnswer(invocation -> {
            TransactionModel m = invocation.getArgument(0);
            m.setId("saved-trx-id");
            return m;
        });

        TransactionResponseDTO response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("saved-trx-id");
        assertThat(response.getCustomerName()).isEqualTo(customer.getName());
        verify(transactionRepositoryPort).save(any(TransactionModel.class));
    }

    @Test
    @DisplayName("givenExistingId_whenGetTransactionById_thenReturnResponseDTO")
    void givenExistingId_whenGetTransactionById_thenReturnResponseDTO() {
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        when(transactionRepositoryPort.findById(trx.getId())).thenReturn(Optional.of(trx));

        TransactionResponseDTO response = transactionService.getTransactionById(trx.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(trx.getId());
    }

    @Test
    @DisplayName("givenMissingId_whenGetTransactionById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetTransactionById_thenThrowDataNotFoundException() {
        when(transactionRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Transaksi tidak ditemukan");
    }

    @Test
    @DisplayName("givenFilterAndPageable_whenGetAllTransactions_thenReturnPagedTransactions")
    void givenFilterAndPageable_whenGetAllTransactions_thenReturnPagedTransactions() {
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        Page<TransactionModel> page = new PageImpl<>(List.of(trx), PageRequest.of(0, 10), 1);
        TransactionFilter filter = TransactionFilter.builder().build();

        when(transactionRepositoryPort.findAll(filter, PageRequest.of(0, 10))).thenReturn(page);

        Page<TransactionResponseDTO> result = transactionService.getAllTransactions(filter, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("givenUpdateRequestWithNewAndExistingDetails_whenUpdateTransaction_thenSaveAndReturnResponse")
    void givenUpdateRequestWithNewAndExistingDetails_whenUpdateTransaction_thenSaveAndReturnResponse() {
        TransactionModel existingTrx = TestDataBuilder.createSampleTransaction();
        PackageModel pkg = TestDataBuilder.createSamplePackage();

        TransactionDetailModel existingDetail = existingTrx.getTransactionDetails().get(0);
        existingDetail.setTargetDate(LocalDateTime.now().plusDays(2));

        TransactionDetailRequestDTO detailDTO1 = new TransactionDetailRequestDTO();
        detailDTO1.setId(existingDetail.getId());
        detailDTO1.setPackageId(pkg.getId());
        detailDTO1.setQuantity(3);
        detailDTO1.setOrderDate(LocalDateTime.now().plusHours(1));

        TransactionDetailRequestDTO detailDTO2 = new TransactionDetailRequestDTO();
        detailDTO2.setId(null); // new detail
        detailDTO2.setPackageId(pkg.getId());
        detailDTO2.setQuantity(1);
        detailDTO2.setOrderDate(LocalDateTime.now().plusHours(1));

        TransactionRequestDTO updateRequest = new TransactionRequestDTO();
        updateRequest.setStatus(TransactionStatus.DONE);
        updateRequest.setOrderDate(LocalDateTime.now().plusHours(1));
        updateRequest.setTransactionDetails(List.of(detailDTO1, detailDTO2));

        when(transactionRepositoryPort.findById(existingTrx.getId())).thenReturn(Optional.of(existingTrx));
        when(packageUseCase.getPackageByIds(any())).thenReturn(Map.of(pkg.getId(), pkg));
        when(transactionDetailUseCase.getByTransactionIdMap(existingTrx.getId()))
                .thenReturn(new java.util.HashMap<>(Map.of(existingDetail.getId(), existingDetail)));
        when(transactionRepositoryPort.save(any(TransactionModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO result = transactionService.updateTransaction(existingTrx.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.DONE);
    }

    @Test
    @DisplayName("givenMissingTransaction_whenUpdateTransaction_thenThrowDataNotFoundException")
    void givenMissingTransaction_whenUpdateTransaction_thenThrowDataNotFoundException() {
        when(transactionRepositoryPort.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction("unknown-id", new TransactionRequestDTO()))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Transaksi tidak ditemukan");
    }

    @Test
    @DisplayName("givenTransactionWithFiles_whenDeleteTransaction_thenDeleteFilesAndRepoRecord")
    void givenTransactionWithFiles_whenDeleteTransaction_thenDeleteFilesAndRepoRecord() {
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        TransactionFileModel fileModel = TestDataBuilder.createSampleTransactionFile();
        trx.setTransactionFiles(List.of(fileModel));

        when(transactionRepositoryPort.findById(trx.getId())).thenReturn(Optional.of(trx));

        transactionService.deleteTransaction(trx.getId());

        verify(transactionFileUseCase).deleteListFile(any(TransactionFileDeleteRequestDTO.class));
        verify(transactionRepositoryPort).deleteById(trx.getId());
    }

    @Test
    @DisplayName("givenTransactionWithoutFiles_whenDeleteTransaction_thenOnlyDeleteRepoRecord")
    void givenTransactionWithoutFiles_whenDeleteTransaction_thenOnlyDeleteRepoRecord() {
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        trx.setTransactionFiles(null);

        when(transactionRepositoryPort.findById(trx.getId())).thenReturn(Optional.of(trx));

        transactionService.deleteTransaction(trx.getId());

        verify(transactionRepositoryPort).deleteById(trx.getId());
        verifyNoInteractions(transactionFileUseCase);
    }
}
