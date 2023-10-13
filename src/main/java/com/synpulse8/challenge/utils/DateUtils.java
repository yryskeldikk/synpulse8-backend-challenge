package com.synpulse8.challenge.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Date getStartDate(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        return calendar.getTime();
    }

    public static Date getEndDate(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

}
