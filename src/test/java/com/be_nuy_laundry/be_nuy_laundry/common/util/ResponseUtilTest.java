package com.be_nuy_laundry.be_nuy_laundry.common.util;

import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ErrorResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseUtilTest {

    @Test
    @DisplayName("givenData_whenSuccessDefaultStatus_thenReturnOkResponse")
    void givenData_whenSuccessDefaultStatus_thenReturnOkResponse() {
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.success("Operation success", "payload");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getMessage()).isEqualTo("Operation success");
        assertThat(response.getBody().getData()).isEqualTo("payload");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("givenDataAndStatus_whenSuccessCustomStatus_thenReturnStatusResponse")
    void givenDataAndStatus_whenSuccessCustomStatus_thenReturnStatusResponse() {
        ResponseEntity<ApiResponse<Integer>> response = ResponseUtil.success("Created successfully", 100, HttpStatus.CREATED);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Created successfully");
        assertThat(response.getBody().getData()).isEqualTo(100);
    }

    @Test
    @DisplayName("givenPage_whenSuccessPage_thenReturnPageResponse")
    void givenPage_whenSuccessPage_thenReturnPageResponse() {
        List<String> items = List.of("item1", "item2");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 10), 2);

        ResponseEntity<ApiResponse<PageResponse<String>>> response = ResponseUtil.successPage("Fetch page success", page);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        PageResponse<String> pageResponse = response.getBody().getData();
        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent()).containsExactly("item1", "item2");
        assertThat(pageResponse.getPage()).isEqualTo(0);
        assertThat(pageResponse.getSize()).isEqualTo(10);
        assertThat(pageResponse.getTotalElements()).isEqualTo(2);
        assertThat(pageResponse.getTotalPages()).isEqualTo(1);
        assertThat(pageResponse.isLast()).isTrue();
    }

    @Test
    @DisplayName("givenErrorMessage_whenErrorResponseSimple_thenReturnErrorResponse")
    void givenErrorMessage_whenErrorResponseSimple_thenReturnErrorResponse() {
        ResponseEntity<ErrorResponse<String>> response = ResponseUtil.errorResponse("Resource not found", HttpStatus.NOT_FOUND);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("FAILED");
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getErrors()).isNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("givenErrorsPayload_whenErrorResponseWithErrors_thenReturnErrorResponseWithPayload")
    void givenErrorsPayload_whenErrorResponseWithErrors_thenReturnErrorResponseWithPayload() {
        Map<String, String> errors = Map.of("email", "email is required");
        ResponseEntity<ErrorResponse<Map<String, String>>> response =
                ResponseUtil.errorResponse("Validation failed", errors, HttpStatus.BAD_REQUEST);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors()).isEqualTo(errors);
    }

    @Test
    @DisplayName("Constructor invocation for coverage")
    void testConstructor() {
        ResponseUtil util = new ResponseUtil();
        assertThat(util).isNotNull();
    }
}
