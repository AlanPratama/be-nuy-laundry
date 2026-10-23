package com.be_nuy_laundry.be_nuy_laundry.common.util;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValueUtilTest {

    @Test
    @DisplayName("givenVariousStrings_whenHasValueString_thenReturnExpectedBoolean")
    void givenVariousStrings_whenHasValueString_thenReturnExpectedBoolean() {
        assertThat(ValueUtil.hasValueString("validString")).isTrue();
        assertThat(ValueUtil.hasValueString("  valid with spaces  ")).isTrue();

        assertThat(ValueUtil.hasValueString(null)).isFalse();
        assertThat(ValueUtil.hasValueString("")).isFalse();
        assertThat(ValueUtil.hasValueString("   ")).isFalse();
    }

    @Test
    @DisplayName("givenObjects_whenHasValueObject_thenReturnExpectedBoolean")
    void givenObjects_whenHasValueObject_thenReturnExpectedBoolean() {
        assertThat(ValueUtil.hasValueObject(new Object())).isTrue();
        assertThat(ValueUtil.hasValueObject("text")).isTrue();
        assertThat(ValueUtil.hasValueObject(null)).isFalse();
    }

    @Test
    @DisplayName("givenLists_whenHasValueList_thenReturnExpectedBoolean")
    void givenLists_whenHasValueList_thenReturnExpectedBoolean() {
        assertThat(ValueUtil.hasValueList(List.of("item1"))).isTrue();
        assertThat(ValueUtil.hasValueList(new ArrayList<>())).isFalse();
        assertThat(ValueUtil.hasValueList(null)).isFalse();
    }

    @Test
    @DisplayName("givenMultipartFiles_whenHasValueMultipartFile_thenReturnExpectedBoolean")
    void givenMultipartFiles_whenHasValueMultipartFile_thenReturnExpectedBoolean() {
        MultipartFile validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThat(ValueUtil.hasValueMultipartFile(validFile)).isTrue();
        assertThat(ValueUtil.hasValueMultipartFile(emptyFile)).isFalse();
        assertThat(ValueUtil.hasValueMultipartFile(null)).isFalse();
    }

    @Test
    @DisplayName("givenValuesAndDefaults_whenOrElseMethods_thenReturnExpectedValue")
    void givenValuesAndDefaults_whenOrElseMethods_thenReturnExpectedValue() {
        // orElseString
        assertThat(ValueUtil.orElseString("actual", "default")).isEqualTo("actual");
        assertThat(ValueUtil.orElseString(null, "default")).isEqualTo("default");
        assertThat(ValueUtil.orElseString("   ", "default")).isEqualTo("default");

        // orElseUserRole
        assertThat(ValueUtil.orElseUserRole(UserRole.ADMIN, UserRole.STAFF)).isEqualTo(UserRole.ADMIN);
        assertThat(ValueUtil.orElseUserRole(null, UserRole.STAFF)).isEqualTo(UserRole.STAFF);

        // orElseInteger
        assertThat(ValueUtil.orElseInteger(42, 10)).isEqualTo(42);
        assertThat(ValueUtil.orElseInteger(null, 10)).isEqualTo(10);

        // orElseLocalDateTime
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fallback = now.minusDays(1);
        assertThat(ValueUtil.orElseLocalDateTime(now, fallback)).isEqualTo(now);
        assertThat(ValueUtil.orElseLocalDateTime(null, fallback)).isEqualTo(fallback);
    }

    @Test
    @DisplayName("Constructor invocation for coverage")
    void testConstructor() {
        ValueUtil util = new ValueUtil();
        assertThat(util).isNotNull();
    }
}
