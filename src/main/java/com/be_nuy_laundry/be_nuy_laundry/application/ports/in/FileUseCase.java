package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUseCase {
    String upload(MultipartFile file);
    String upload(MultipartFile file, String fileName);
    String upload(MultipartFile file, String fileName, String folder);

    void delete(String fileName);
    List<MinIoRemoveObjectErrorResponse> deleteObjects(List<String> fileNames);
    byte[] download(String fileName);
}
