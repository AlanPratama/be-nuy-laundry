package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusUpdateRequestDTO {

    @NotNull(message = "Status transaksi tidak boleh kosong")
    private TransactionStatus status;
}
