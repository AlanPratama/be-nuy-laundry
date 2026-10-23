package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.TransactionFileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileUploadRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file.TransactionFileDeleteResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionFileControllerTest {

    @Mock
    private TransactionFileUseCase transactionFileUseCase;

    @InjectMocks
    private TransactionFileController transactionFileController;

    @Test
    @DisplayName("givenValidRequestAndFiles_whenUpload_thenReturnTransactionFiles")
    void givenValidRequestAndFiles_whenUpload_thenReturnTransactionFiles() {
        TransactionFileUploadRequestDTO request = new TransactionFileUploadRequestDTO();
        request.setTransactionId("trx-1");

        MultipartFile receipt = new MockMultipartFile("receipt", "receipt.png", "image/png", new byte[]{1});
        MultipartFile invoice = new MockMultipartFile("invoice", "invoice.pdf", "application/pdf", new byte[]{2});
        List<MultipartFile> beforeList = List.of(new MockMultipartFile("before", "before.png", "image/png", new byte[]{3}));
        List<MultipartFile> afterList = List.of(new MockMultipartFile("after", "after.png", "image/png", new byte[]{4}));
        List<MultipartFile> otherList = List.of(new MockMultipartFile("other", "other.png", "image/png", new byte[]{5}));

        List<TransactionFileModel> models = List.of(TestDataBuilder.createSampleTransactionFile());
        when(transactionFileUseCase.uploadTransactionFile(request)).thenReturn(models);

        ResponseEntity<ApiResponse<List<TransactionFileModel>>> response = transactionFileController.upload(
                request, receipt, invoice, beforeList, afterList, otherList
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(models);
        assertThat(request.getPaymentReceiptFile()).isEqualTo(receipt);
        assertThat(request.getInvoiceDocumentFile()).isEqualTo(invoice);
        assertThat(request.getConditionBeforeFiles()).isEqualTo(beforeList);
        assertThat(request.getConditionAfterFiles()).isEqualTo(afterList);
        assertThat(request.getOtherFiles()).isEqualTo(otherList);
        verify(transactionFileUseCase).uploadTransactionFile(request);
    }

    @Test
    @DisplayName("givenFileId_whenDelete_thenReturnSuccessResponse")
    void givenFileId_whenDelete_thenReturnSuccessResponse() {
        doNothing().when(transactionFileUseCase).deleteTransactionFile("file-1");

        ResponseEntity<ApiResponse<Void>> response = transactionFileController.delete("file-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(transactionFileUseCase).deleteTransactionFile("file-1");
    }

    @Test
    @DisplayName("givenDeleteListRequest_whenDeleteList_thenReturnDeleteResponses")
    void givenDeleteListRequest_whenDeleteList_thenReturnDeleteResponses() {
        TransactionFileDeleteRequestDTO request = new TransactionFileDeleteRequestDTO();
        request.setTransactionFileIds(List.of("file-1", "file-2"));

        TransactionFileDeleteResponseDTO responseItem = new TransactionFileDeleteResponseDTO();
        responseItem.setMessage("Deleted");
        List<TransactionFileDeleteResponseDTO> deleteResponses = List.of(responseItem);

        when(transactionFileUseCase.deleteListTransactionFile(request)).thenReturn(deleteResponses);

        ResponseEntity<ApiResponse<List<TransactionFileDeleteResponseDTO>>> response =
                transactionFileController.deleteList(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(deleteResponses);
        verify(transactionFileUseCase).deleteListTransactionFile(request);
    }
}
