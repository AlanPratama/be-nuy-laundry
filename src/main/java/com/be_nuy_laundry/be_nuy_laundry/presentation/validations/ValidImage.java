package com.be_nuy_laundry.be_nuy_laundry.presentation.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FileValidator.class, FileListValidator.class})
public @interface ValidImage {

    String message() default "File tidak valid. Hanya menerima JPG/PNG.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean required() default true;
}
