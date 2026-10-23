package com.be_nuy_laundry.be_nuy_laundry.domain.utils;

import com.be_nuy_laundry.be_nuy_laundry.common.util.DateUtil;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransactionNumberGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String DATE_FORMAT = "ddMMyyyy";
    private static final Integer UNIQUE_CHAR_LENGTH = 14;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateInvoiceNo() {
        LocalDate now = LocalDate.now();

        String hari = DateUtil.getShortDay(now.getDayOfWeek().getValue());
        String tanggal = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String randomChar = generateRandomString(UNIQUE_CHAR_LENGTH);

        return String.format("TRX-%s-%s-%s", hari, tanggal, randomChar).toUpperCase();
    }

    private static String generateRandomString(Integer length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}