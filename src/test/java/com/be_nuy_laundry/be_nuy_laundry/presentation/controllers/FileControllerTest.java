package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileUseCase fileUseCase;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FileController fileController;

    @Test
    @DisplayName("givenEmptyPath_whenViewFile_thenReturnNotFound")
    void givenEmptyPath_whenViewFile_thenReturnNotFound() {
        ResponseEntity<byte[]> response = fileController.viewFileByParam("");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<byte[]> nullResponse = fileController.viewFileByParam(null);
        assertThat(nullResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("givenJpgFile_whenViewFile_thenReturnJpegContentType")
    void givenJpgFile_whenViewFile_thenReturnJpegContentType() {
        byte[] dummy = "jpeg content".getBytes();
        when(fileUseCase.download("photos/test.jpg")).thenReturn(dummy);

        ResponseEntity<byte[]> response = fileController.viewFileByParam("photos/test.jpg");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(response.getBody()).isEqualTo(dummy);
    }

    @Test
    @DisplayName("givenPngAndWebpAndPdfFiles_whenViewFile_thenReturnRespectiveTypes")
    void givenPngAndWebpAndPdfFiles_whenViewFile_thenReturnRespectiveTypes() {
        byte[] dummy = "content".getBytes();

        when(fileUseCase.download("photos/test.png")).thenReturn(dummy);
        ResponseEntity<byte[]> pngResp = fileController.viewFileByParam("photos/test.png");
        assertThat(pngResp.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);

        when(fileUseCase.download("photos/test.webp")).thenReturn(dummy);
        ResponseEntity<byte[]> webpResp = fileController.viewFileByParam("photos/test.webp");
        assertThat(webpResp.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("image/webp"));

        when(fileUseCase.download("docs/test.pdf")).thenReturn(dummy);
        ResponseEntity<byte[]> pdfResp = fileController.viewFileByParam("docs/test.pdf");
        assertThat(pdfResp.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);

        when(fileUseCase.download("docs/test.bin")).thenReturn(dummy);
        ResponseEntity<byte[]> binResp = fileController.viewFileByParam("docs/test.bin");
        assertThat(binResp.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    @DisplayName("givenDownloadException_whenViewFile_thenReturnNotFound")
    void givenDownloadException_whenViewFile_thenReturnNotFound() {
        when(fileUseCase.download("photos/missing.jpg")).thenThrow(new RuntimeException("File not found"));

        ResponseEntity<byte[]> response = fileController.viewFileByParam("photos/missing.jpg");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("givenPathInRequest_whenViewFileByPath_thenReturnOk")
    void givenPathInRequest_whenViewFileByPath_thenReturnOk() {
        byte[] dummy = "image data".getBytes();
        when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
                .thenReturn("/api/v1/files/sample/path.jpg");
        when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .thenReturn("/api/v1/files/**");
        when(fileUseCase.download("sample/path.jpg")).thenReturn(dummy);

        ResponseEntity<byte[]> response = fileController.viewFileByPath(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dummy);
    }
}
