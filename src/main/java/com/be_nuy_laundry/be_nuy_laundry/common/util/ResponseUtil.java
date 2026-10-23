package com.be_nuy_laundry.be_nuy_laundry.common.util;

import com.be_nuy_laundry.be_nuy_laundry.common.constant.Constant;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ErrorResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return success(message, data, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, HttpStatus httpStatus) {
        ApiResponse<T> response = new ApiResponse();
        response.setStatus(Constant.SUCCESS_STRING);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(message);
        response.setData(data);

        return ResponseEntity.status(httpStatus).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<PageResponse<T>>> successPage(String message, Page<T> page) {
        PageResponse<T> pageResponse = PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        ApiResponse<PageResponse<T>> response = new ApiResponse<>();
        response.setStatus(Constant.SUCCESS_STRING);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        response.setData(pageResponse);

        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<ErrorResponse<String>> errorResponse(String message, HttpStatus httpStatus) {
        ErrorResponse<String> errorResponse = ErrorResponse.<String>builder()
                .status(Constant.FAILED_STRING)
                .message(message)
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
    public static <T> ResponseEntity<ErrorResponse<T>> errorResponse(String message, T errors, HttpStatus httpStatus) {
        ErrorResponse<T> errorResponse = ErrorResponse.<T>builder()
                .status(Constant.FAILED_STRING)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

}
