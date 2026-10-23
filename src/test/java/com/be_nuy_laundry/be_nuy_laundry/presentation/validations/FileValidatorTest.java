package com.be_nuy_laundry.be_nuy_laundry.presentation.validations;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileValidatorTest {

    private FileValidator fileValidator;

    @Mock
    private ValidImage validImageAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();
    }

    @Test
    @DisplayName("givenNullOrEmptyFile_whenRequiredIsFalse_thenReturnTrue")
    void givenNullOrEmptyFile_whenRequiredIsFalse_thenReturnTrue() {
        when(validImageAnnotation.required()).thenReturn(false);
        fileValidator.initialize(validImageAnnotation);

        assertThat(fileValidator.isValid(null, context)).isTrue();

        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        assertThat(fileValidator.isValid(emptyFile, context)).isTrue();
    }

    @Test
    @DisplayName("givenNullOrEmptyFile_whenRequiredIsTrue_thenReturnFalseAndBuildConstraintViolation")
    void givenNullOrEmptyFile_whenRequiredIsTrue_thenReturnFalseAndBuildConstraintViolation() {
        when(validImageAnnotation.required()).thenReturn(true);
        fileValidator.initialize(validImageAnnotation);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean result = fileValidator.isValid(null, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("File gambar wajib diupload");
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("givenValidJpegAndPngFiles_thenReturnTrue")
    void givenValidJpegAndPngFiles_thenReturnTrue() {
        when(validImageAnnotation.required()).thenReturn(true);
        fileValidator.initialize(validImageAnnotation);

        MockMultipartFile jpegFile = new MockMultipartFile("file", "pic.jpg", "image/jpeg", "imageContent".getBytes());
        MockMultipartFile pngFile = new MockMultipartFile("file", "pic.png", "image/png", "imageContent".getBytes());

        assertThat(fileValidator.isValid(jpegFile, context)).isTrue();
        assertThat(fileValidator.isValid(pngFile, context)).isTrue();
    }

    @Test
    @DisplayName("givenInvalidContentType_thenReturnFalse")
    void givenInvalidContentType_thenReturnFalse() {
        when(validImageAnnotation.required()).thenReturn(true);
        fileValidator.initialize(validImageAnnotation);

        MockMultipartFile pdfFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdfContent".getBytes());

        assertThat(fileValidator.isValid(pdfFile, context)).isFalse();
    }
}
