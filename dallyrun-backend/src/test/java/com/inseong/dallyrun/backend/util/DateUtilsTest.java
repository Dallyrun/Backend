package com.inseong.dallyrun.backend.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void getWeekStart_midWeek() {
        // 2026-04-15 is Wednesday
        LocalDate date = LocalDate.of(2026, 4, 15);
        LocalDate weekStart = DateUtils.getWeekStart(date);
        assertEquals(LocalDate.of(2026, 4, 13), weekStart); // Monday
    }

    @Test
    void getWeekStart_monday() {
        LocalDate monday = LocalDate.of(2026, 4, 13);
        assertEquals(monday, DateUtils.getWeekStart(monday));
    }

    @Test
    void getWeekStart_sunday() {
        LocalDate sunday = LocalDate.of(2026, 4, 19);
        assertEquals(LocalDate.of(2026, 4, 13), DateUtils.getWeekStart(sunday));
    }

    @Test
    void getWeekEnd_midWeek() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        LocalDate weekEnd = DateUtils.getWeekEnd(date);
        assertEquals(LocalDate.of(2026, 4, 19), weekEnd); // Sunday
    }

    @Test
    void getWeekEnd_sunday() {
        LocalDate sunday = LocalDate.of(2026, 4, 19);
        assertEquals(sunday, DateUtils.getWeekEnd(sunday));
    }

    @Test
    void getMonthStart() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        assertEquals(LocalDate.of(2026, 4, 1), DateUtils.getMonthStart(date));
    }

    @Test
    void getMonthEnd_april() {
        LocalDate date = LocalDate.of(2026, 4, 15);
        assertEquals(LocalDate.of(2026, 4, 30), DateUtils.getMonthEnd(date));
    }

    @Test
    void getMonthEnd_february_nonLeapYear() {
        LocalDate date = LocalDate.of(2026, 2, 10);
        assertEquals(LocalDate.of(2026, 2, 28), DateUtils.getMonthEnd(date));
    }

    @Test
    void getMonthEnd_february_leapYear() {
        LocalDate date = LocalDate.of(2024, 2, 10);
        assertEquals(LocalDate.of(2024, 2, 29), DateUtils.getMonthEnd(date));
    }
}
