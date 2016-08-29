package com.codepath.selfiespot.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String DATE_FORMAT_DETAIL = "h:mm a . dd MMM yy";

    public static String getDetailPageTime(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DETAIL);
        return sdf.format(date);
    }
}
