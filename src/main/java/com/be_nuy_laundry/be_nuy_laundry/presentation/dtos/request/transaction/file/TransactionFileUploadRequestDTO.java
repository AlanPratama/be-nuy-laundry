package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class TransactionFileUploadRequestDTO {

    @NotBlank(message = "Transaction ID tidak boleh kosong")
    private String transactionId;

    private MultipartFile paymentReceiptFile;

    private MultipartFile invoiceDocumentFile;

    private List<MultipartFile> conditionBeforeFiles;

    private List<MultipartFile> conditionAfterFiles;

    private List<MultipartFile> otherFiles;

}
