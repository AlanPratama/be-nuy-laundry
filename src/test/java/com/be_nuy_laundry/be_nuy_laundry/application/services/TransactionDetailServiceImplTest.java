package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionDetailRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionDetailServiceImplTest {

    @Mock
    private TransactionDetailRepositoryPort transactionDetailRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private TransactionDetailServiceImpl transactionDetailService;

    @Test
    @DisplayName("givenTransactionId_whenGetByTransactionIdMap_thenReturnMapOfDetails")
    void givenTransactionId_whenGetByTransactionIdMap_thenReturnMapOfDetails() {
        TransactionDetailModel detail1 = new TransactionDetailModel();
        detail1.setId("det-1");
        TransactionDetailModel detail2 = new TransactionDetailModel();
        detail2.setId("det-2");

        when(transactionDetailRepositoryPort.findByTransactionId("trx-1"))
                .thenReturn(List.of(detail1, detail2));

        Map<String, TransactionDetailModel> result = transactionDetailService.getByTransactionIdMap("trx-1");

        assertThat(result).hasSize(2).containsKeys("det-1", "det-2");
    }

    @Test
    @DisplayName("givenExistingId_whenFindById_thenReturnDetail")
    void givenExistingId_whenFindById_thenReturnDetail() {
        TransactionDetailModel detail = new TransactionDetailModel();
        detail.setId("det-123");

        when(transactionDetailRepositoryPort.findById("det-123")).thenReturn(Optional.of(detail));

        TransactionDetailModel result = transactionDetailService.findById("det-123");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("det-123");
    }

    @Test
    @DisplayName("givenMissingId_whenFindById_thenThrowDataNotFoundException")
    void givenMissingId_whenFindById_thenThrowDataNotFoundException() {
        when(transactionDetailRepositoryPort.findById("missing-det")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionDetailService.findById("missing-det"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Transaksi detail tidak ditemukan");
    }

    @Test
    @DisplayName("givenMissingTransaction_whenDeleteById_thenThrowDataNotFoundException")
    void givenMissingTransaction_whenDeleteById_thenThrowDataNotFoundException() {
        TransactionDetailModel detail = new TransactionDetailModel();
        detail.setId("det-1");
        detail.setTransactionId("missing-trx");

        when(transactionDetailRepositoryPort.findById("det-1")).thenReturn(Optional.of(detail));
        when(transactionRepositoryPort.findById("missing-trx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionDetailService.deleteById("det-1"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Transaksi tidak ditemukan");
    }

    @Test
    @DisplayName("givenOnlyOneDetailRemaining_whenDeleteById_thenThrowConflictException")
    void givenOnlyOneDetailRemaining_whenDeleteById_thenThrowConflictException() {
        TransactionDetailModel detail = new TransactionDetailModel();
        detail.setId("det-1");
        detail.setTransactionId("trx-1");
        detail.setTotalPrice(BigDecimal.valueOf(15000));

        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        trx.setId("trx-1");
        trx.setTransactionDetails(new ArrayList<>(List.of(detail)));

        when(transactionDetailRepositoryPort.findById("det-1")).thenReturn(Optional.of(detail));
        when(transactionRepositoryPort.findById("trx-1")).thenReturn(Optional.of(trx));

        assertThatThrownBy(() -> transactionDetailService.deleteById("det-1"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Minimal harus ada 1 transaksi detail");
    }

    @Test
    @DisplayName("givenMultipleDetails_whenDeleteById_thenSubtractPriceAndSaveTransaction")
    void givenMultipleDetails_whenDeleteById_thenSubtractPriceAndSaveTransaction() {
        TransactionDetailModel detail1 = new TransactionDetailModel();
        detail1.setId("det-1");
        detail1.setTransactionId("trx-1");
        detail1.setTotalPrice(BigDecimal.valueOf(15000));

        TransactionDetailModel detail2 = new TransactionDetailModel();
        detail2.setId("det-2");
        detail2.setTransactionId("trx-1");
        detail2.setTotalPrice(BigDecimal.valueOf(25000));

        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        trx.setId("trx-1");
        trx.setTotalPrice(BigDecimal.valueOf(40000));
        trx.setTransactionDetails(new ArrayList<>(List.of(detail1, detail2)));

        when(transactionDetailRepositoryPort.findById("det-1")).thenReturn(Optional.of(detail1));
        when(transactionRepositoryPort.findById("trx-1")).thenReturn(Optional.of(trx));

        transactionDetailService.deleteById("det-1");

        verify(transactionDetailRepositoryPort).deleteById("det-1");
        assertThat(trx.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(trx.getTransactionDetails()).hasSize(1);
        verify(transactionRepositoryPort).save(trx);
    }
}
