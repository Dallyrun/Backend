package com.inseong.dallyrun.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * 주간/월간 기간 계산 유틸리티.
 * 주 시작은 월요일(ISO-8601), 주 끝은 일요일 기준이다.
 */
public final class DateUtils {

    private DateUtils() {
    }

    /** @return 주어진 날짜가 속한 주의 월요일 (해당일이 월요일이면 그대로 반환) */
    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** @return 주어진 날짜가 속한 주의 일요일 (해당일이 일요일이면 그대로 반환) */
    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /** @return 주어진 날짜가 속한 달의 1일 */
    public static LocalDate getMonthStart(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /** @return 주어진 날짜가 속한 달의 마지막 날 */
    public static LocalDate getMonthEnd(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
}
