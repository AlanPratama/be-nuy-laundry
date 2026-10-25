package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressQueueItemDTO {
    private String id;
    private String invoiceNumber;
    private String customerName;
    private String packageName;
    private Double quantity;
    private String deadlineTime;
    private Long remainingMinutes;
    private String status;
}
