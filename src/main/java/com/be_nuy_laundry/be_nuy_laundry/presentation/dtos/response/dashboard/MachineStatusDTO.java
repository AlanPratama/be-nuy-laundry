package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineStatusDTO {
    private String id;
    private String name;
    private Integer capacityKg;
    private String status; // WASHING, DRYING, IDLE
    private Integer remainingMinutes;
    private String currentInvoice;
}
