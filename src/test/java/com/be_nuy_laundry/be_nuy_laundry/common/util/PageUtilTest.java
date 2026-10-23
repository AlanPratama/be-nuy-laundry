package com.be_nuy_laundry.be_nuy_laundry.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageUtilTest {

    @Test
    @DisplayName("givenDescendingDirection_whenSetPageable_thenReturnDescendingPageable")
    void givenDescendingDirection_whenSetPageable_thenReturnDescendingPageable() {
        Pageable pageable = PageUtil.setPageable(0, 10, "createdAt", "desc");

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.isDescending()).isTrue();
    }

    @Test
    @DisplayName("givenAscendingDirection_whenSetPageable_thenReturnAscendingPageable")
    void givenAscendingDirection_whenSetPageable_thenReturnAscendingPageable() {
        Pageable pageable = PageUtil.setPageable(2, 25, "name", "asc");

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        Sort.Order order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.isAscending()).isTrue();
    }

    @Test
    @DisplayName("Constructor invocation for coverage")
    void testConstructor() {
        PageUtil util = new PageUtil();
        assertThat(util).isNotNull();
    }
}
