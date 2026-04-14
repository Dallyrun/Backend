package com.inseong.dallyrun.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class DateUtils {

    private DateUtils() {
    }

    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDate getMonthStart(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate getMonthEnd(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
}
