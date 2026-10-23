package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionModel extends BaseModel {
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
    private UserModel user;
    private List<TransactionDetailModel> transactionDetails = new ArrayList<>();
    private List<TransactionFileModel> transactionFiles;
}
