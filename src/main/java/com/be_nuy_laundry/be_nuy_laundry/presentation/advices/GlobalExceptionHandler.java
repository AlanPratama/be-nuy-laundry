package com.be_nuy_laundry.be_nuy_laundry.presentation.advices;

import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ErrorResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Map<String, List<String>>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();

            errors.computeIfAbsent(fieldName, key -> new ArrayList<>()).add(errorMessage);
        }

        log.warn("[MethodArgumentNotValidException] Validation failed: {}", errors);

        return ResponseUtil.errorResponse("Request validation", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().iterator().next().getMessage();

        log.warn("[ConstraintViolationException] Constraint violation: {}", errorMessage);

        return ResponseUtil.errorResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse<String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.warn("[MaxUploadSizeExceededException] File upload exceeded max size");

        return ResponseUtil.errorResponse("Ukuran file terlalu besar. Maksimal 3MB.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleDataNotFoundException(DataNotFoundException ex) {
        log.warn("[DataNotFoundException] Data not found: {}", ex.getMessage());

        return ResponseUtil.errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse<String>> handleForbiddenException(ForbiddenException ex) {
        log.warn("[ForbiddenException] Forbidden: {}", ex.getMessage());

        return ResponseUtil.errorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<String>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("[AccessDeniedException] Access denied: {}", ex.getMessage());

        return ResponseUtil.errorResponse("Anda tidak memiliki hak akses untuk fitur ini", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse<String>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("[UnauthorizedException] Unauthorized: {}", ex.getMessage());

        return ResponseUtil.errorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse<String>> handleConflictException(ConflictException ex) {
        log.warn("[ConflictException] Conflict: {}", ex.getMessage());

        return ResponseUtil.errorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<String>> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseUtil.errorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

