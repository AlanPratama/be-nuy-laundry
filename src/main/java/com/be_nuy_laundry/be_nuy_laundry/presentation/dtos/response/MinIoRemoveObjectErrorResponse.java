package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MinIoRemoveObjectErrorResponse {

    private String bucketName;
    private String objectName;
    private String message;

}
