package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionFileRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.TransactionRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileUploadRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file.TransactionFileDeleteResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionFileServiceImplTest {

    @Mock
    private FileUseCase fileUseCase;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private TransactionFileRepositoryPort transactionFileRepositoryPort;

    @InjectMocks
    private TransactionFileServiceImpl transactionFileService;

    @Test
    @DisplayName("givenExistingId_whenGetTransactionFileById_thenReturnFileModel")
    void givenExistingId_whenGetTransactionFileById_thenReturnFileModel() {
        TransactionFileModel model = TestDataBuilder.createSampleTransactionFile();
        when(transactionFileRepositoryPort.findById(model.getId())).thenReturn(Optional.of(model));

        TransactionFileModel result = transactionFileService.getTransactionFileById(model.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(model.getId());
    }

    @Test
    @DisplayName("givenMissingId_whenGetTransactionFileById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetTransactionFileById_thenThrowDataNotFoundException() {
        when(transactionFileRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionFileService.getTransactionFileById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("File tidak ditemukan");
    }

    @Test
    @DisplayName("givenMatchingIds_whenGetTransactionFileByIds_thenReturnList")
    void givenMatchingIds_whenGetTransactionFileByIds_thenReturnList() {
        TransactionFileModel model1 = TestDataBuilder.createSampleTransactionFile();
        model1.setId("id-1");
        TransactionFileModel model2 = TestDataBuilder.createSampleTransactionFile();
        model2.setId("id-2");

        when(transactionFileRepositoryPort.findByIds(List.of("id-1", "id-2")))
                .thenReturn(List.of(model1, model2));

        List<TransactionFileModel> result = transactionFileService
                .getTransactionFileByIds(List.of("id-1", "id-2", "id-1"));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("givenMismatchedIds_whenGetTransactionFileByIds_thenThrowDataNotFoundException")
    void givenMismatchedIds_whenGetTransactionFileByIds_thenThrowDataNotFoundException() {
        TransactionFileModel model1 = TestDataBuilder.createSampleTransactionFile();
        model1.setId("id-1");

        when(transactionFileRepositoryPort.findByIds(List.of("id-1", "id-2")))
                .thenReturn(List.of(model1));

        assertThatThrownBy(() -> transactionFileService.getTransactionFileByIds(List.of("id-1", "id-2")))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Beberapa file tidak ditemukan");
    }

    @Test
    @DisplayName("givenInvoiceNoNotFound_whenUploadTransactionFile_thenThrowDataNotFoundException")
    void givenInvoiceNoNotFound_whenUploadTransactionFile_thenThrowDataNotFoundException() {
        TransactionFileUploadRequestDTO request = new TransactionFileUploadRequestDTO();
        request.setTransactionId("trx-123");

        when(transactionRepositoryPort.getInvoiceNo("trx-123")).thenReturn(null);

        assertThatThrownBy(() -> transactionFileService.uploadTransactionFile(request))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Transaksi tidak ditemukan");
    }

    @Test
    @DisplayName("givenInvoiceNoFound_whenUploadTransactionFile_thenSaveFiles")
    void givenInvoiceNoFound_whenUploadTransactionFile_thenSaveFiles() {
        TransactionFileUploadRequestDTO request = new TransactionFileUploadRequestDTO();
        request.setTransactionId("trx-123");
        request.setPaymentReceiptFile(new MockMultipartFile("pay", "pay.jpg", "image/jpeg", "content".getBytes()));

        when(transactionRepositoryPort.getInvoiceNo("trx-123")).thenReturn("TRX-123");
        when(fileUseCase.upload(any(), anyString(), anyString())).thenReturn("/path/pay.jpg");
        when(transactionFileRepositoryPort.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TransactionFileModel> result = transactionFileService.uploadTransactionFile(request);

        assertThat(result).hasSize(1);
        verify(transactionFileRepositoryPort).saveAll(any());
    }

    @Test
    @DisplayName("givenDeleteListTransactionFileWithPartialFailures_thenReturnResponsesAndPruneDatabase")
    void givenDeleteListTransactionFileWithPartialFailures_thenReturnResponsesAndPruneDatabase() {
        TransactionFileModel file1 = TestDataBuilder.createSampleTransactionFile();
        file1.setId("id-1");
        file1.setFilePath("transactions/f1.jpg");

        TransactionFileModel file2 = TestDataBuilder.createSampleTransactionFile();
        file2.setId("id-2");
        file2.setFilePath("transactions/f2.jpg");

        TransactionFileDeleteRequestDTO request = new TransactionFileDeleteRequestDTO();
        request.setTransactionFileIds(List.of("id-1", "id-2"));

        when(transactionFileRepositoryPort.findByIds(List.of("id-1", "id-2"))).thenReturn(List.of(file1, file2));

        MinIoRemoveObjectErrorResponse err = new MinIoRemoveObjectErrorResponse();
        err.setObjectName("transactions/f1.jpg");
        err.setBucketName("test-bucket");
        err.setMessage("MinIO error");

        when(fileUseCase.deleteObjects(List.of("transactions/f1.jpg", "transactions/f2.jpg")))
                .thenReturn(List.of(err));

        List<TransactionFileDeleteResponseDTO> responses = transactionFileService.deleteListTransactionFile(request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getObjectName()).isEqualTo("transactions/f1.jpg");
        // id-2 succeeded, so it should be deleted from DB
        verify(transactionFileRepositoryPort).deleteByIds(List.of("id-2"));
    }

    @Test
    @DisplayName("givenDeleteListFile_thenReturnMinioErrorResponses")
    void givenDeleteListFile_thenReturnMinioErrorResponses() {
        TransactionFileModel file1 = TestDataBuilder.createSampleTransactionFile();
        file1.setId("id-1");
        file1.setFilePath("transactions/f1.jpg");

        TransactionFileDeleteRequestDTO request = new TransactionFileDeleteRequestDTO();
        request.setTransactionFileIds(List.of("id-1"));

        when(transactionFileRepositoryPort.findByIds(List.of("id-1"))).thenReturn(List.of(file1));

        MinIoRemoveObjectErrorResponse err = new MinIoRemoveObjectErrorResponse();
        err.setObjectName("transactions/f1.jpg");
        err.setBucketName("test-bucket");
        err.setMessage("MinIO error");

        when(fileUseCase.deleteObjects(List.of("transactions/f1.jpg"))).thenReturn(List.of(err));

        List<TransactionFileDeleteResponseDTO> responses = transactionFileService.deleteListFile(request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMessage()).isEqualTo("MinIO error");
        verify(transactionFileRepositoryPort, never()).deleteByIds(any());
    }

    @Test
    @DisplayName("givenExistingFileId_whenDeleteTransactionFile_thenDeleteFromStorageAndDB")
    void givenExistingFileId_whenDeleteTransactionFile_thenDeleteFromStorageAndDB() {
        TransactionFileModel file = TestDataBuilder.createSampleTransactionFile();
        when(transactionFileRepositoryPort.findById(file.getId())).thenReturn(Optional.of(file));

        transactionFileService.deleteTransactionFile(file.getId());

        verify(fileUseCase).delete(file.getFilePath());
        verify(transactionFileRepositoryPort).deleteById(file.getId());
    }

    @Test
    @DisplayName("givenAllFileTypes_whenSetTransactionFiles_thenMapAllUploadedFiles")
    void givenAllFileTypes_whenSetTransactionFiles_thenMapAllUploadedFiles() {
        MockMultipartFile receipt = new MockMultipartFile("receipt", "rec.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile invoice = new MockMultipartFile("invoice", "inv.pdf", "application/pdf",
                "content".getBytes());
        MockMultipartFile before = new MockMultipartFile("before", "bef.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile after = new MockMultipartFile("after", "aft.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile other = new MockMultipartFile("other", "oth.jpg", "image/jpeg", "content".getBytes());

        when(fileUseCase.upload(any(), anyString(), anyString())).thenReturn("/uploaded/path");

        List<TransactionFileModel> result = transactionFileService.setTransactionFiles(
                "TRX-001",
                receipt,
                invoice,
                List.of(before),
                List.of(after),
                List.of(other));

        assertThat(result).hasSize(5);
    }
}
