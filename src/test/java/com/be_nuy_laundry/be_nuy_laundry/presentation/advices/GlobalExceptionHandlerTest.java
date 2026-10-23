package com.be_nuy_laundry.be_nuy_laundry.presentation.advices;

import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("givenMethodArgumentNotValidException_whenHandleValidationExceptions_thenReturnBadRequestWithMappedErrors")
    void givenMethodArgumentNotValidException_whenHandleValidationExceptions_thenReturnBadRequestWithMappedErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("loginRequest", "username", "Username is required");
        FieldError fieldError2 = new FieldError("loginRequest", "username", "Username must be alphanumeric");
        FieldError fieldError3 = new FieldError("loginRequest", "password", "Password is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));

        ResponseEntity<ErrorResponse<Map<String, List<String>>>> response =
                exceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Request validation");
        Map<String, List<String>> errors = response.getBody().getErrors();
        assertThat(errors).containsKeys("username", "password");
        assertThat(errors.get("username")).containsExactly("Username is required", "Username must be alphanumeric");
        assertThat(errors.get("password")).containsExactly("Password is required");
    }

    @Test
    @DisplayName("givenConstraintViolationException_whenHandleConstraintViolation_thenReturnBadRequest")
    void givenConstraintViolationException_whenHandleConstraintViolation_thenReturnBadRequest() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid constraint field");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid constraint field");
    }

    @Test
    @DisplayName("givenMaxUploadSizeExceededException_whenHandleMaxSizeException_thenReturnBadRequest")
    void givenMaxUploadSizeExceededException_whenHandleMaxSizeException_thenReturnBadRequest() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(3145728);

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleMaxSizeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Maksimal 3MB");
    }

    @Test
    @DisplayName("givenDataNotFoundException_whenHandleDataNotFoundException_thenReturnNotFound")
    void givenDataNotFoundException_whenHandleDataNotFoundException_thenReturnNotFound() {
        DataNotFoundException ex = new DataNotFoundException("Customer tidak ditemukan");

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleDataNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Customer tidak ditemukan");
    }

    @Test
    @DisplayName("givenForbiddenException_whenHandleForbiddenException_thenReturnForbidden")
    void givenForbiddenException_whenHandleForbiddenException_thenReturnForbidden() {
        ForbiddenException ex = new ForbiddenException("Akun tidak aktif");

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleForbiddenException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Akun tidak aktif");
    }

    @Test
    @DisplayName("givenUnauthorizedException_whenHandleUnauthorizedException_thenReturnUnauthorized")
    void givenUnauthorizedException_whenHandleUnauthorizedException_thenReturnUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Token invalid");

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleUnauthorizedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Token invalid");
    }

    @Test
    @DisplayName("givenConflictException_whenHandleConflictException_thenReturnConflict")
    void givenConflictException_whenHandleConflictException_thenReturnConflict() {
        ConflictException ex = new ConflictException("User sudah tersedia");

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleConflictException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User sudah tersedia");
    }

    @Test
    @DisplayName("givenGenericException_whenHandleGeneralException_thenReturnInternalServerError")
    void givenGenericException_whenHandleGeneralException_thenReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Database connection timeout");

        ResponseEntity<ErrorResponse<String>> response = exceptionHandler.handleGeneralException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
    }
}
