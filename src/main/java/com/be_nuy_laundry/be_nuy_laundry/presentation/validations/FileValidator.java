package com.be_nuy_laundry.be_nuy_laundry.presentation.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private boolean isRequired;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png");

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        this.isRequired = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            if (!isRequired)
                return true;

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File gambar wajib diupload").addConstraintViolation();
            return false;
        }

        return ALLOWED_CONTENT_TYPES.contains(file.getContentType());
    }

}
