package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.FileStoragePort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileUseCase {

    private final FileStoragePort fileStoragePort;

    @Override
    public String upload(MultipartFile file) {
        try {
            return this.upload(file, file.getOriginalFilename());
        } catch (Exception e) {
            throw new RuntimeException("Upload file failed: " + e);
        }
    }

    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            return this.upload(file, fileName, MinIoConstant.FOLDER.GENERAL);
        } catch (Exception e) {
            throw new RuntimeException("Upload file failed: " + e);
        }
    }

    @Override
    public String upload(MultipartFile file, String fileName, String folder) {
        try {
            fileName = folder + "/" + UUID.randomUUID() + "-" + fileName;

            return fileStoragePort.upload(
                    file.getBytes(),
                    fileName,
                    file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Upload file failed: " + e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            fileStoragePort.delete(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Delete file failed: " + e);
        }
    }

    @Override
    public List<MinIoRemoveObjectErrorResponse> deleteObjects(List<String> fileNames) {
        try {
            return fileStoragePort.deleteObjects(fileNames);
        } catch (Exception e) {
            throw new RuntimeException("Delete files failed: " + e);
        }
    }

    @Override
    public byte[] download(String fileName) {
        try {
            return fileStoragePort.download(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Download file failed: " + e);
        }
    }
}
