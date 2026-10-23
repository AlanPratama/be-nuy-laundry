package com.be_nuy_laundry.be_nuy_laundry.domain.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionNumberGeneratorTest {

    private static final Pattern INVOICE_PATTERN = Pattern.compile("^TRX-[A-Z]{3}-\\d{8}-[A-Z0-9]{14}$");

    @Test
    @DisplayName("givenCallGenerateInvoiceNo_thenReturnProperlyFormattedInvoice")
    void givenCallGenerateInvoiceNo_thenReturnProperlyFormattedInvoice() {
        String invoiceNo = TransactionNumberGenerator.generateInvoiceNo();

        assertThat(invoiceNo).isNotNull();
        assertThat(INVOICE_PATTERN.matcher(invoiceNo).matches()).isTrue();
    }

    @Test
    @DisplayName("givenConsecutiveGenerations_thenReturnUniqueInvoiceNumbers")
    void givenConsecutiveGenerations_thenReturnUniqueInvoiceNumbers() {
        Set<String> invoiceSet = new HashSet<>();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            invoiceSet.add(TransactionNumberGenerator.generateInvoiceNo());
        }

        assertThat(invoiceSet).hasSize(iterations);
    }

    @Test
    @DisplayName("Constructor invocation for coverage")
    void testConstructor() {
        TransactionNumberGenerator generator = new TransactionNumberGenerator();
        assertThat(generator).isNotNull();
    }
}
