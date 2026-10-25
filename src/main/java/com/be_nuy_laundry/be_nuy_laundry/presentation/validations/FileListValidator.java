package com.be_nuy_laundry.be_nuy_laundry.presentation.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileListValidator implements ConstraintValidator<ValidImage, List<MultipartFile>> {

    private boolean isRequired;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        this.isRequired = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return !isRequired;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("File gambar tidak valid: " + file.getOriginalFilename())
                            .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
