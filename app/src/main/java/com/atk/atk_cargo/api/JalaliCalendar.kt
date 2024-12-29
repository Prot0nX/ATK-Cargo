package com.atk.atk_cargo.api

import android.annotation.SuppressLint
import java.util.Calendar

class JalaliCalendar {
    private var calendar: Calendar

    constructor() {
        calendar = Calendar.getInstance()
    }

    constructor(calendar: Calendar) {
        this.calendar = calendar
    }

    constructor(year: Int, month: Int, day: Int) {
        calendar = Calendar.getInstance()
        setDate(year, month, day)
    }

    fun setDate(year: Int, month: Int, day: Int) {
        val gregorianDate = jalaliToGregorian(year, month, day)
        calendar.set(gregorianDate[0], gregorianDate[1] - 1, gregorianDate[2])
    }

    fun toGregorian(): Calendar {
        return calendar
    }

    @SuppressLint("DefaultLocale")
    override fun toString(): String {
        val jalaliDate = gregorianToJalali(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        return String.format("%04d/%02d/%02d", jalaliDate[0], jalaliDate[1], jalaliDate[2])
    }

    companion object {
        fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
            val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            var jy: Int
            val jm: Int
            val jd: Int
            if (gy > 1600) {
                jy = 979
                var gy2 = gy - 1600
                jy += 33 * (gy2 / 33)
                gy2 %= 33
                if (gy2 >= 28) {
                    jy += (gy2 - 28) / 4
                    if (gy2 % 4 == 3) jy++
                }
            } else {
                jy = 0
                var gy2 = gy - 621
                jy += 33 * (gy2 / 33)
                gy2 %= 33
                if (gy2 >= 28) {
                    jy += (gy2 - 28) / 4
                    if (gy2 % 4 == 3) jy++
                }
            }
            var days = (365 * gy) +
                    ((gy + 3) / 4) -
                    ((gy + 99) / 100) +
                    ((gy + 399) / 400) -
                    80 +
                    gd +
                    g_d_m[gm - 1]
            jy += 33 * (days / 12053)
            days %= 12053
            jy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                jy += (days - 1) / 365
                days = (days - 1) % 365
            }
            if (days < 186) {
                jm = 1 + days / 31
                jd = 1 + (days % 31)
            } else {
                jm = 7 + (days - 186) / 30
                jd = 1 + ((days - 186) % 30)
            }
            return intArrayOf(jy, jm, jd)
        }

        fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
            var gy: Int
            var gm: Int
            var gd: Int
            var days: Int
            gy = if (jy <= 979) {
                621
            } else {
                1600
            }
            var remainingJy = if (jy <= 979) jy - 1 else jy - 979
            days = (365 * remainingJy) + ((remainingJy / 33) * 8) + (((remainingJy % 33) + 3) / 4) + 78 + jd + (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
            gy += 400 * (days / 146097)
            days %= 146097
            if (days > 36524) {
                gy += 100 * (--days / 36524)
                days %= 36524
                if (days >= 365) days++
            }
            gy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                gy += ((days - 1) / 365)
                days = (days - 1) % 365
            }
            gd = days + 1
            val salA = intArrayOf(0, 31, if (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            gm = 0
            while (gm < 13 && gd > salA[gm]) {
                gd -= salA[gm]
                gm++
            }
            return intArrayOf(gy, gm, gd)
        }
    }
}