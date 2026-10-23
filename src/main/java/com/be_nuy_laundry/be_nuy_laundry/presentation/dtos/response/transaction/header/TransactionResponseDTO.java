package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BaseModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionDetailModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionFileModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponseDTO extends BaseModel {
    private String invoiceNo;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private LocalDateTime targetDate;
    private LocalDateTime deliveredDate;
    private TransactionStatus status;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private CustomerModel customer;
    private UserResponseDTO user;
    private List<TransactionDetailModel> transactionDetails;
    private List<TransactionFileModel> transactionFiles;
}
