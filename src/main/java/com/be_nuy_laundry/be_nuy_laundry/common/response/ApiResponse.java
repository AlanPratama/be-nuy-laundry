package com.be_nuy_laundry.be_nuy_laundry.common.response;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private String status;
    private String message;
    private LocalDateTime timestamp;
    private T data;
}
