package com.be_nuy_laundry.be_nuy_laundry.common.util;

import com.be_nuy_laundry.be_nuy_laundry.common.constant.Constant;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ValueUtil {

    public static Boolean hasValueString(String value) {
        return (value != null && !value.trim().isBlank() && !Constant.EMPTY_STRING.equals(value.trim()))
                ? Boolean.TRUE
                : Boolean.FALSE;
    }

    public static Boolean hasValueObject(Object value) {
        return Objects.nonNull(value) ? Boolean.TRUE : Boolean.FALSE;
    }

    public static <T> Boolean hasValueList(List<T> value) {
        return Objects.nonNull(value) && !value.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
    }

    public static Boolean hasValueMultipartFile(MultipartFile file) {
        return file != null && !file.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
    }

    public static String orElseString(String value, String defaulValue) {
        return ValueUtil.hasValueString(value) ? value : defaulValue;
    }

    public static UserRole orElseUserRole(UserRole value, UserRole defaulValue) {
        return ValueUtil.hasValueObject(value) ? value : defaulValue;
    }

    public static Integer orElseInteger(Integer value, Integer defaulValue) {
        return Objects.nonNull(value) ? value : defaulValue;
    }

    public static LocalDateTime orElseLocalDateTime(LocalDateTime value, LocalDateTime defaulValue) {
        return Objects.nonNull(value) ? value : defaulValue;
    }

}
