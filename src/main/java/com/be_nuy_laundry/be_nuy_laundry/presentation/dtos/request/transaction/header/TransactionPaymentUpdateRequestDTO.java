package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.transaction.header;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPaymentUpdateRequestDTO {

    @DecimalMin(value = "0.0", message = "Jumlah pembayaran tidak boleh negatif")
    private BigDecimal downPayment;

    private PaymentStatus paymentStatus;
}
