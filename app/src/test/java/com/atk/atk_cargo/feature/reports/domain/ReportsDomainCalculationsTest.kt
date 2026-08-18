package com.atk.atk_cargo.feature.reports.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsDomainCalculationsTest {

    // ===== calculatePercentage =====

    @Test
    fun calculatePercentage_normalRatio_roundsDownToInt() {
        assertEquals(50, calculatePercentage(50f, 100f))
        assertEquals(33, calculatePercentage(1f, 3f))
    }

    @Test
    fun calculatePercentage_zeroTotal_returnsZeroInsteadOfDividingByZero() {
        assertEquals(0, calculatePercentage(10f, 0f))
    }

    @Test
    fun calculatePercentage_valueExceedsTotal_isClampedTo100() {
        assertEquals(100, calculatePercentage(150f, 100f))
    }

    @Test
    fun calculatePercentage_negativeValue_isClampedToZero() {
        assertEquals(0, calculatePercentage(-10f, 100f))
    }

    // ===== calculateProgress =====

    @Test
    fun calculateProgress_normalRatio_returnsFractionRoundedToTwoDecimals() {
        assertEquals(0.5f, calculateProgress(50f, 100f), 0.001f)
        assertEquals(0.33f, calculateProgress(1f, 3f), 0.001f)
    }

    @Test
    fun calculateProgress_zeroTotal_returnsZeroInsteadOfDividingByZero() {
        assertEquals(0f, calculateProgress(10f, 0f), 0.001f)
    }

    @Test
    fun calculateProgress_valueExceedsTotal_isClampedToOne() {
        assertEquals(1f, calculateProgress(150f, 100f), 0.001f)
    }

    @Test
    fun calculateProgress_negativeValue_isClampedToZero() {
        assertEquals(0f, calculateProgress(-10f, 100f), 0.001f)
    }

    // ===== formatNumber =====

    @Test
    fun formatNumber_addsThousandsSeparators() {
        assertEquals("1,234,567", formatNumber(1234567))
    }

    @Test
    fun formatNumber_smallNumber_noSeparator() {
        assertEquals("42", formatNumber(42))
    }

    @Test
    fun formatNumber_zero_returnsZero() {
        assertEquals("0", formatNumber(0))
    }

    // ===== formatWeightWithDetail =====

    @Test
    fun formatWeightWithDetail_belowOneThousandKg_showsKilos() {
        assertEquals("500 کیلو", formatWeightWithDetail(500f))
    }

    @Test
    fun formatWeightWithDetail_exactTons_showsWholeTons() {
        assertEquals("5 تن", formatWeightWithDetail(5000f))
    }

    @Test
    fun formatWeightWithDetail_fractionalTons_showsOneDecimal() {
        assertEquals("5.5 تن", formatWeightWithDetail(5500f))
    }

    @Test
    fun formatWeightWithDetail_exactThousandTons_showsWholeThousandTons() {
        assertEquals("2 هزار تن", formatWeightWithDetail(2_000_000f))
    }
}
