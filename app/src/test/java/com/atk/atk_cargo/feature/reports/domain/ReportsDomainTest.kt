package com.atk.atk_cargo.feature.reports.domain

import com.atk.atk_cargo.api.Ship
import com.atk.atk_cargo.api.ShipSortingMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsDomainTest {

    private fun ship(
        name: String,
        totalTonnage: Float,
        remainingTonnage: Float
    ) = Ship(
        name = name,
        warehouseCount = 1,
        quotaCount = 1,
        totalTonnage = totalTonnage,
        remainingTonnage = remainingTonnage,
        isActive = true
    )

    @Test
    fun sortShips_byRemainingTonnageAsc_ordersFromSmallestToLargest() {
        val ships = listOf(
            ship("A", totalTonnage = 100f, remainingTonnage = 30f),
            ship("B", totalTonnage = 100f, remainingTonnage = 10f),
            ship("C", totalTonnage = 100f, remainingTonnage = 20f)
        )

        val sorted = sortShips(ships, ShipSortingMode.REMAINING_TONNAGE_ASC)

        assertEquals(listOf("B", "C", "A"), sorted.map { it.name })
    }

    @Test
    fun sortShips_byLoadedTonnageDesc_ordersByTotalMinusRemaining() {
        val ships = listOf(
            ship("A", totalTonnage = 100f, remainingTonnage = 90f), // loaded = 10
            ship("B", totalTonnage = 100f, remainingTonnage = 10f), // loaded = 90
            ship("C", totalTonnage = 100f, remainingTonnage = 50f)  // loaded = 50
        )

        val sorted = sortShips(ships, ShipSortingMode.LOADED_TONNAGE_DESC)

        assertEquals(listOf("B", "C", "A"), sorted.map { it.name })
    }

    @Test
    fun sortShips_byNameAsc_usesPersianAlphabeticalOrder() {
        val ships = listOf(
            ship("کشتی ب", totalTonnage = 100f, remainingTonnage = 10f),
            ship("کشتی آ", totalTonnage = 100f, remainingTonnage = 10f),
            ship("کشتی ت", totalTonnage = 100f, remainingTonnage = 10f)
        )

        val sorted = sortShips(ships, ShipSortingMode.NAME_ASC)

        assertEquals(listOf("کشتی آ", "کشتی ب", "کشتی ت"), sorted.map { it.name })
    }

    @Test
    fun sortShips_byNameDesc_reversesAlphabeticalOrder() {
        val ships = listOf(
            ship("کشتی آ", totalTonnage = 100f, remainingTonnage = 10f),
            ship("کشتی ت", totalTonnage = 100f, remainingTonnage = 10f)
        )

        val sorted = sortShips(ships, ShipSortingMode.NAME_DESC)

        assertEquals(listOf("کشتی ت", "کشتی آ"), sorted.map { it.name })
    }

    @Test
    fun sortShips_doesNotMutateOriginalList() {
        val ships = listOf(
            ship("B", totalTonnage = 100f, remainingTonnage = 20f),
            ship("A", totalTonnage = 100f, remainingTonnage = 10f)
        )
        val originalOrder = ships.map { it.name }

        sortShips(ships, ShipSortingMode.REMAINING_TONNAGE_ASC)

        assertEquals(originalOrder, ships.map { it.name })
    }
}
