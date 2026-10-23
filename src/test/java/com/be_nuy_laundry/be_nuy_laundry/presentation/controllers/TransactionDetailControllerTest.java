package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionDetailUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionDetailControllerTest {

    @Mock
    private TransactionDetailUseCase transactionDetailUseCase;

    @InjectMocks
    private TransactionDetailController transactionDetailController;

    @Test
    @DisplayName("givenDetailId_whenDelete_thenReturnSuccessResponse")
    void givenDetailId_whenDelete_thenReturnSuccessResponse() {
        doNothing().when(transactionDetailUseCase).deleteById("detail-1");

        ResponseEntity<ApiResponse<Void>> response = transactionDetailController.delete("detail-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(transactionDetailUseCase).deleteById("detail-1");
    }
}
