package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;

import java.util.List;

public interface FileStoragePort {
    String upload(byte[] file, String fileName, String contentType);
    void delete(String fileName);
    List<MinIoRemoveObjectErrorResponse> deleteObjects(List<String> fileNames);
    byte[] download(String fileName);
}
