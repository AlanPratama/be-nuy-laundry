package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.FileStoragePort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("givenMultipartFile_whenUpload_thenCallStorageWithGeneralFolder")
    void givenMultipartFile_whenUpload_thenCallStorageWithGeneralFolder() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "imageContent".getBytes());

        when(fileStoragePort.upload(any(), contains("test.jpg"), eq("image/jpeg"))).thenReturn("general/uuid-test.jpg");

        String result = fileService.upload(file);

        assertThat(result).contains("test.jpg");
    }

    @Test
    @DisplayName("givenMultipartFileAndCustomName_whenUpload_thenCallStorageWithCustomName")
    void givenMultipartFileAndCustomName_whenUpload_thenCallStorageWithCustomName() {
        MockMultipartFile file = new MockMultipartFile("file", "original.jpg", "image/jpeg", "imageContent".getBytes());

        when(fileStoragePort.upload(any(), contains("custom.jpg"), eq("image/jpeg"))).thenReturn("general/uuid-custom.jpg");

        String result = fileService.upload(file, "custom.jpg");

        assertThat(result).contains("custom.jpg");
    }

    @Test
    @DisplayName("givenFileAndFolder_whenUpload_thenCallStorageWithGivenFolder")
    void givenFileAndFolder_whenUpload_thenCallStorageWithGivenFolder() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.png", "image/png", "content".getBytes());

        when(fileStoragePort.upload(any(), contains(MinIoConstant.FOLDER.TRANSACTION), eq("image/png")))
                .thenReturn(MinIoConstant.FOLDER.TRANSACTION + "/uuid-doc.png");

        String result = fileService.upload(file, "doc.png", MinIoConstant.FOLDER.TRANSACTION);

        assertThat(result).contains(MinIoConstant.FOLDER.TRANSACTION);
    }

    @Test
    @DisplayName("givenFailingFile_whenUpload_thenThrowRuntimeException")
    void givenFailingFile_whenUpload_thenThrowRuntimeException() throws IOException {
        MockMultipartFile brokenFile = mock(MockMultipartFile.class);
        when(brokenFile.getBytes()).thenThrow(new IOException("Disk read error"));

        assertThatThrownBy(() -> fileService.upload(brokenFile, "broken.jpg", "folder"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload file failed");
    }

    @Test
    @DisplayName("givenFailingFile_whenUpload1Arg_thenThrowRuntimeException")
    void givenFailingFile_whenUpload1Arg_thenThrowRuntimeException() {
        MockMultipartFile brokenFile = mock(MockMultipartFile.class);
        when(brokenFile.getOriginalFilename()).thenThrow(new RuntimeException("Filename error"));

        assertThatThrownBy(() -> fileService.upload(brokenFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload file failed");
    }

    @Test
    @DisplayName("givenFailingFile_whenUpload2Arg_thenThrowRuntimeException")
    void givenFailingFile_whenUpload2Arg_thenThrowRuntimeException() throws IOException {
        MockMultipartFile brokenFile = mock(MockMultipartFile.class);
        when(brokenFile.getBytes()).thenThrow(new IOException("Disk read error"));

        assertThatThrownBy(() -> fileService.upload(brokenFile, "broken.jpg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload file failed");
    }

    @Test
    @DisplayName("givenFileName_whenDelete_thenCallStorageDelete")
    void givenFileName_whenDelete_thenCallStorageDelete() {
        fileService.delete("general/file.png");

        verify(fileStoragePort).delete("general/file.png");
    }

    @Test
    @DisplayName("givenStorageException_whenDelete_thenThrowRuntimeException")
    void givenStorageException_whenDelete_thenThrowRuntimeException() {
        doThrow(new RuntimeException("MinIO unavailable")).when(fileStoragePort).delete(anyString());

        assertThatThrownBy(() -> fileService.delete("fail.png"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Delete file failed");
    }

    @Test
    @DisplayName("givenFileNamesList_whenDeleteObjects_thenReturnErrorList")
    void givenFileNamesList_whenDeleteObjects_thenReturnErrorList() {
        MinIoRemoveObjectErrorResponse err = new MinIoRemoveObjectErrorResponse();
        err.setObjectName("f1.png");
        when(fileStoragePort.deleteObjects(List.of("f1.png"))).thenReturn(List.of(err));

        List<MinIoRemoveObjectErrorResponse> result = fileService.deleteObjects(List.of("f1.png"));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("givenStorageException_whenDeleteObjects_thenThrowRuntimeException")
    void givenStorageException_whenDeleteObjects_thenThrowRuntimeException() {
        when(fileStoragePort.deleteObjects(any())).thenThrow(new RuntimeException("Error bulk"));

        assertThatThrownBy(() -> fileService.deleteObjects(List.of("f1.png")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Delete files failed");
    }

    @Test
    @DisplayName("givenFileName_whenDownload_thenReturnByteArray")
    void givenFileName_whenDownload_thenReturnByteArray() {
        byte[] expected = "fileContent".getBytes();
        when(fileStoragePort.download("file.pdf")).thenReturn(expected);

        byte[] result = fileService.download("file.pdf");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("givenStorageException_whenDownload_thenThrowRuntimeException")
    void givenStorageException_whenDownload_thenThrowRuntimeException() {
        when(fileStoragePort.download(anyString())).thenThrow(new RuntimeException("Not found"));

        assertThatThrownBy(() -> fileService.download("missing.pdf"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Download file failed");
    }
}
