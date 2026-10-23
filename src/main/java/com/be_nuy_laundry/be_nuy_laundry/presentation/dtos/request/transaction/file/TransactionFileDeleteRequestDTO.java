package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.file;

import lombok.Data;

import java.util.List;

@Data
public class TransactionFileDeleteRequestDTO {

    private List<String> transactionFileIds;

}
