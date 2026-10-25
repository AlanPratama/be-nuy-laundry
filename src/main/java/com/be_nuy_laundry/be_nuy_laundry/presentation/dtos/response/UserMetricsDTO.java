package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMetricsDTO {
    private long totalStaff;
    private long activeStaff;
    private long inactiveStaff;
    private long adminCount;
    private long cashierCount;
}
