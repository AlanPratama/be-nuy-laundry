package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionFileType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionFileModel extends BaseModel {
    private String filePath;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private TransactionFileType fileType;
    private TransactionModel transaction;
}
