package com.be_nuy_laundry.be_nuy_laundry.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilTest {

    @ParameterizedTest(name = "Day of week {0} should return {1}")
    @CsvSource({
            "1, SEN",
            "2, SEL",
            "3, RAB",
            "4, KAM",
            "5, JUM",
            "6, SAB",
            "7, MIN"
    })
    @DisplayName("givenValidDayOfWeek_whenGetShortDay_thenReturnIndonesianAbbreviation")
    void givenValidDayOfWeek_whenGetShortDay_thenReturnIndonesianAbbreviation(int dayOfWeek, String expected) {
        String result = DateUtil.getShortDay(dayOfWeek);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("givenInvalidDayOfWeek_whenGetShortDay_thenReturnUNK")
    void givenInvalidDayOfWeek_whenGetShortDay_thenReturnUNK() {
        assertThat(DateUtil.getShortDay(0)).isEqualTo("UNK");
        assertThat(DateUtil.getShortDay(8)).isEqualTo("UNK");
        assertThat(DateUtil.getShortDay(-1)).isEqualTo("UNK");
    }

    @Test
    @DisplayName("Constructor invocation for coverage")
    void testConstructor() {
        DateUtil util = new DateUtil();
        assertThat(util).isNotNull();
    }
}
