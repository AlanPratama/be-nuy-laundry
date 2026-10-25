package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.detail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDetailRequestDTO {

    private String id;

    @NotBlank(message = "Paket tidak boleh kosong")
    private String packageId;

    @NotNull(message = "Kuantitas tidak boleh kosong")
    private Integer quantity;

    @NotNull(message = "Tanggal estimasi tidak boleh kosong")
    private LocalDateTime orderDate;

}
