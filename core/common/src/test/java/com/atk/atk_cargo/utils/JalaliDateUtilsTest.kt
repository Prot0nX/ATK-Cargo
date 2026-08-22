package com.atk.atk_cargo.utils

import org.junit.Assert.assertEquals
import org.junit.Test

// مقادیر مرجع با کتابخانه‌ی پایتون jdatetime تأیید شدند تا تست خودش را تأیید نکند.
class JalaliDateUtilsTest {

    @Test
    fun formatDate_dateOnlyInput_convertsNowruz1403Correctly() {
        // 1403/01/01 (نوروز) = 2024-03-20 در تقویم میلادی
        assertEquals("1403/01/01", JalaliDateUtils.formatDate("2024-03-20"))
    }

    @Test
    fun formatDate_dateOnlyInput_convertsNowruz1402Correctly() {
        assertEquals("1402/01/01", JalaliDateUtils.formatDate("2023-03-21"))
    }

    @Test
    fun formatDate_leapYearEsfand30_convertsCorrectly() {
        // ۱۴۰۳ سال کبیسه است؛ اسفند آن ۳۰ روز دارد (2025-03-20 میلادی)
        assertEquals("1403/12/30", JalaliDateUtils.formatDate("2025-03-20"))
    }

    @Test
    fun formatDate_dayBeforeLeapEsfand30_convertsCorrectly() {
        assertEquals("1403/12/29", JalaliDateUtils.formatDate("2025-03-19"))
    }

    @Test
    fun formatDate_nonLeapYearEsfand29_rollsOverToNewYear() {
        // ۱۴۰۴ کبیسه نیست؛ اسفند آن ۲۹ روز دارد
        assertEquals("1404/12/29", JalaliDateUtils.formatDate("2026-03-20"))
        assertEquals("1405/01/01", JalaliDateUtils.formatDate("2026-03-21"))
    }

    @Test
    fun formatDate_millenniumReference_convertsCorrectly() {
        assertEquals("1378/10/11", JalaliDateUtils.formatDate("2000-01-01"))
    }

    @Test
    fun formatDate_dateTimeInput_ignoresTimeComponent() {
        assertEquals("1403/01/01", JalaliDateUtils.formatDate("2024-03-20 10:15:00"))
    }

    @Test
    fun formatDate_numericEpochMillis_convertsUsingTehranTimeZone() {
        // 1710917100000 = 2024-03-20T10:15:00+03:30 (بدون DST؛ ایران از ۱۴۰۱ DST را حذف کرده)
        assertEquals("1403/01/01", JalaliDateUtils.formatDate("1710917100000"))
    }

    @Test
    fun formatDate_numericEpochMillis_leapEsfand30() {
        // 1742502540000 = 2025-03-20T23:59:00+03:30
        assertEquals("1403/12/30", JalaliDateUtils.formatDate("1742502540000"))
    }

    @Test
    fun formatDate_invalidInput_returnsInputUnchanged() {
        val invalid = "not-a-date"
        assertEquals(invalid, JalaliDateUtils.formatDate(invalid))
    }

    @Test
    fun formatDate_trimsWhitespace() {
        assertEquals("1403/01/01", JalaliDateUtils.formatDate("  2024-03-20  "))
    }

    @Test
    fun formatTime_dateTimeInput_extractsHourAndMinute() {
        assertEquals("10:15", JalaliDateUtils.formatTime("2024-03-20 10:15:00"))
    }

    @Test
    fun formatTime_numericEpochMillis_usesTehranTimeZone() {
        assertEquals("10:15", JalaliDateUtils.formatTime("1710917100000"))
    }

    @Test
    fun formatTime_invalidInput_returnsEmptyString() {
        assertEquals("", JalaliDateUtils.formatTime("not-a-date"))
    }

    @Test
    fun getCurrentJalaliDateString_returnsWellFormedJalaliDate() {
        val result = JalaliDateUtils.getCurrentJalaliDateString()
        assertEquals(true, Regex("""\d{4}/\d{2}/\d{2}""").matches(result))
    }
}
