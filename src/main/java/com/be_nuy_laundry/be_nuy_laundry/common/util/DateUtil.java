package com.be_nuy_laundry.be_nuy_laundry.common.util;

public class DateUtil {

    public static String getShortDay(int dayOfWeek) {
        return switch (dayOfWeek) {

            // TRX-SEN-22012026-1KDOE9L3PNIAK
            case 1 -> "SEN";
            case 2 -> "SEL";
            case 3 -> "RAB";
            case 4 -> "KAM";
            case 5 -> "JUM";
            case 6 -> "SAB";
            case 7 -> "MIN";
            default -> "UNK";
        };
    }

}
