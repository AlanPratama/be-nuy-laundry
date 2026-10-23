package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileDeleteRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file.TransactionFileUploadRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file.TransactionFileDeleteResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TransactionFileUseCase {
    TransactionFileModel getTransactionFileById(String id);
    List<TransactionFileModel> getTransactionFileByIds(List<String> ids);
    List<TransactionFileModel> uploadTransactionFile(TransactionFileUploadRequestDTO request);
    void deleteTransactionFile(String id);
    List<TransactionFileDeleteResponseDTO> deleteListTransactionFile(TransactionFileDeleteRequestDTO request);
    List<TransactionFileDeleteResponseDTO> deleteListFile(TransactionFileDeleteRequestDTO request);
    List<TransactionFileModel> setTransactionFiles(
            String invoiceNo,
            MultipartFile paymentReceiptFile,
            MultipartFile invoiceDocumentFile,
            List<MultipartFile> conditionBeforeFiles,
            List<MultipartFile> conditionAfterFiles,
            List<MultipartFile> otherFiles);
}
