package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.file;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFileDeleteResponseDTO {
    private TransactionFileModel transactionFile;
    private String bucketName;
    private String objectName;
    private String message;
}