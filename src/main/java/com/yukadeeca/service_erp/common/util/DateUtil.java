package com.yukadeeca.service_erp.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public final class DateUtil {
    private DateUtil() {
        // Prevent instantiation
    }

    private static final ZoneOffset ZONE_OFF_SET = ZoneOffset.UTC;

    public static LocalDateTime nowDate() {
        return LocalDateTime.now(ZONE_OFF_SET);
    }

    public static String nowAsString() {
        return nowDate().toString();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZONE_OFF_SET));
    }

}
