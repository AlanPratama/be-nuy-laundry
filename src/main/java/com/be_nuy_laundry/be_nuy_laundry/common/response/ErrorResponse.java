package com.be_nuy_laundry.be_nuy_laundry.common.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse<T> {

    private String status;
    private String message;
    private LocalDateTime timestamp;
    private T errors;
    
}
