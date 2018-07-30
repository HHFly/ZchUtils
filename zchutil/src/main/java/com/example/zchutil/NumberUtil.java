package com.example.zchutil;

public class NumberUtil {
    public NumberUtil() {
    }

    public static long millisecondToSecond(long millisecond) {
        long second = millisecond / 1000L;
        if (millisecond % 1000L != 0L) {
            ++second;
        }

        return second;
    }
}
