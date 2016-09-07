package com.codepath.selfiespot.util;

public class NumberUtils {

    public static String withSuffix(final int count) {
        if (count < 1000) {
            return "" + count;
        }
        final int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "KMGTPE".charAt(exp - 1));
    }
}
