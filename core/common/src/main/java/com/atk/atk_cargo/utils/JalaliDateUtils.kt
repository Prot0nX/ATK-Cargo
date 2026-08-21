package com.atk.atk_cargo.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object JalaliDateUtils {

    fun formatDate(timestampStr: String): String {
        val input = timestampStr.trim()
        try {
            val instant = when {
                input.length <= 10 && input.contains("-") -> {
                    // YYYY-MM-DD
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val ld = java.time.LocalDate.parse(input, formatter)
                    ld.atStartOfDay(ZoneId.of("Asia/Tehran")).toInstant()
                }
                input.contains("-") -> {
                    // YYYY-MM-DD HH:mm:ss
                    val cleanInput = if (input.length > 19) input.take(19) else input
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val ldt = java.time.LocalDateTime.parse(cleanInput, formatter)
                    ldt.atZone(ZoneId.of("Asia/Tehran")).toInstant()
                }
                else -> {
                    // Timestamp (Numeric)
                    Instant.ofEpochMilli(input.toLong())
                }
            }
            
            val zdt = instant.atZone(ZoneId.of("Asia/Tehran"))
            val jDate = gregorianToJalali(zdt.year, zdt.monthValue, zdt.dayOfMonth)
            
            val year = jDate.year
            val month = jDate.month.toString().padStart(2, '0')
            val day = jDate.day.toString().padStart(2, '0')
            
            val result = "$year/$month/$day"
            return result
        } catch (e: Exception) {
            return input
        }
    }

    fun formatTime(timestampStr: String): String {
        val input = timestampStr.trim()
        try {
            val instant = if (input.contains("-")) {
                 val cleanInput = if (input.length > 19) input.take(19) else input
                 val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                 val ldt = java.time.LocalDateTime.parse(cleanInput, formatter)
                 ldt.atZone(ZoneId.of("Asia/Tehran")).toInstant()
            } else {
                 Instant.ofEpochMilli(input.toLong())
            }
            val zdt = instant.atZone(ZoneId.of("Asia/Tehran"))
            val hour = zdt.hour.toString().padStart(2, '0')
            val minute = zdt.minute.toString().padStart(2, '0')
            
            val result = "$hour:$minute"
            return result
        } catch (e: Exception) {
            return ""
        }
    }

    // تاریخ جلالی امروز به فرمت YYYY/MM/DD بر اساس منطقه‌ی زمانی Tehran، مستقل از تنظیم دستگاه.
    fun getCurrentJalaliDateString(): String {
        val zdt = java.time.ZonedDateTime.now(ZoneId.of("Asia/Tehran"))
        val jDate = gregorianToJalali(zdt.year, zdt.monthValue, zdt.dayOfMonth)
        val month = jDate.month.toString().padStart(2, '0')
        val day = jDate.day.toString().padStart(2, '0')
        return "${jDate.year}/$month/$day"
    }

    data class JalaliDate(val year: Int, val month: Int, val day: Int)

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy_ = gy - 1600
        var gm_ = gm - 1
        var gd_ = gd - 1

        var gDayNo = 365 * gy_ + (gy_ + 3) / 4 - (gy_ + 99) / 100 + (gy_ + 399) / 400

        for (i in 0 until gm_) {
            gDayNo += gDaysInMonth[i]
        }
        
        // Leap year check for February
        if (gm_ > 1 && ((gy_ % 4 == 0 && gy_ % 100 != 0) || (gy_ % 400 == 0))) {
            gDayNo++
        }
        
        gDayNo += gd_

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)

        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        
        for (i in 0..10) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= jDaysInMonth[i]
        }
        
        if (jm == 0) {
            jm = 12
            jd = jDayNo + 1
        }

        return JalaliDate(jy, jm, jd)
    }
}
