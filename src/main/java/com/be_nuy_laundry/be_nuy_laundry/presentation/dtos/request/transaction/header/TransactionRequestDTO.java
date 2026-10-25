package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.detail.TransactionDetailRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionRequestDTO {

    @NotBlank(message = "Pelanggan harus diisi")
    private String customerId;

    @NotBlank(message = "User harus diisi")
    private String userId;

    @NotNull(message = "Tanggal order tidak boleh kosong")
    private LocalDateTime orderDate;

    private LocalDateTime targetDate;

    @NotNull(message = "Status transaksi tidak boleh kosong")
    private TransactionStatus status;

    private BigDecimal downPayment;

    private PaymentStatus paymentStatus;

    // DETAILS
    @NotNull(message = "Transaksi detail tidak boleh kosong")
    private List<TransactionDetailRequestDTO> transactionDetails;

    // FILES
    private MultipartFile paymentReceiptFile;

    private MultipartFile invoiceDocumentFile;

    private List<MultipartFile> conditionBeforeFiles;

    private List<MultipartFile> conditionAfterFiles;

    private List<MultipartFile> otherFiles;

}
