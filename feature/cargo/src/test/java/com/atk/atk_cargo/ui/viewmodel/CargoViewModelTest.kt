package com.atk.atk_cargo.ui.viewmodel

import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.InitialInfo
import com.atk.atk_cargo.data.model.MessageType
import com.atk.atk_cargo.domain.repository.QuotaRepository
import com.atk.atk_cargo.domain.session.UserPreferencesStore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// پوشش محدود به منطقی که بدون دستگاه/امولاتور قابل تست است؛ متدهایی که مستقیم apiServiceV2 (نه repository تزریق‌شده) را صدا می‌زنند در JVM با UnsatisfiedLinkError شکست می‌خورند و خارج از دامنه ماندند
@OptIn(ExperimentalCoroutinesApi::class)
class CargoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: QuotaRepository
    private lateinit var userPreferencesManager: UserPreferencesStore
    private lateinit var viewModel: CargoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
        userPreferencesManager = mockk(relaxed = true)
        // ioDispatcher = همان StandardTestDispatcher، وگرنه withContext(Dispatchers.IO) به ترد پس‌زمینه‌ی واقعی می‌رفت که advanceUntilIdle() با آن هماهنگ نمی‌شود
        viewModel = CargoViewModel(repository, userPreferencesManager, ioDispatcher = dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun cargoInfo(
        id: Int? = 1,
        trackingNumber: String = "T-1",
        exitTime: String? = null,
        exitDate: String? = null
    ) = CargoInfo(
        id = id,
        trackingNumber = trackingNumber,
        numberOfPeople = "1",
        username = "user1",
        userType = "operator",
        entryTime = "10:00:00",
        netWeight = "",
        scaleReceiptNumber = "",
        shortageWeight = "",
        excessWeight = "",
        exitTime = exitTime,
        exitDate = exitDate,
        status = "ورود",
        shipName = "Ship-1",
        loadingWarehouse = "WH-1",
        cargoType = "Type-1",
        shippingCompany = "Co-1",
        loadingQuotaNumber = "Q-1",
        confirm = "no"
    )

    private fun initialInfo() = InitialInfo(
        shipName = "Ship-1",
        loadingWarehouse = "WH-1",
        cargoType = "Type-1",
        shippingCompany = "Co-1",
        cargoWeight = 100f,
        loadingQuotaNumber = 1,
        remainingWeight = 900f,
        totalNetWeight = 100f,
        averageNetWeight = 50f,
        remainingServices = 5
    )

    private fun stubCargoInfo(vararg items: CargoInfo) {
        coEvery {
            repository.getCargoInfo(any(), any(), any(), any())
        } returns CargoInfoResponse(cargoInfoList = items.toList(), initialInfo = initialInfo())
    }

    private fun load() {
        viewModel.loadCargoInfoList(
            quotaNumber = "Q-1",
            shippingCompany = "Co-1",
            warehouse = "WH-1",
            cargoType = "Type-1"
        )
        dispatcher.scheduler.advanceUntilIdle()
    }

    // ===== loadCargoInfoList =====

    @Test
    fun `loadCargoInfoList - success maps CargoInfo to Cargo and stores initialInfo`() = runTest(dispatcher) {
        stubCargoInfo(cargoInfo(id = 1, trackingNumber = "ABC-100"))

        load()

        val state = viewModel.uiState.value
        assertEquals(1, state.cargoInfoList.size)
        assertEquals("ABC-100", state.cargoInfoList.first().trackingNumber)
        assertEquals("Ship-1", state.initialInfo?.shipName)
    }

    @Test
    fun `loadCargoInfoList - repository failure surfaces error snackbar, not crash`() = runTest(dispatcher) {
        coEvery {
            repository.getCargoInfo(any(), any(), any(), any())
        } throws RuntimeException("network down")

        load()

        assertTrue(viewModel.showAnimatedMessage.value)
        assertEquals(MessageType.ERROR, viewModel.messageType.value)
        assertTrue(viewModel.uiState.value.cargoInfoList.isEmpty())
    }

    @Test
    fun `loadCargoInfoList - duplicate tracking numbers trigger duplicate dialog`() = runTest(dispatcher) {
        stubCargoInfo(
            cargoInfo(id = 1, trackingNumber = "SAME"),
            cargoInfo(id = 2, trackingNumber = "SAME")
        )

        load()

        val state = viewModel.uiState.value
        assertEquals(CargoDialog.Duplicates(listOf("SAME")), state.dialog)
        assertEquals(2, state.cargoInfoList.size)
    }

    @Test
    fun `loadCargoInfoList - no duplicates leaves duplicate dialog closed`() = runTest(dispatcher) {
        stubCargoInfo(
            cargoInfo(id = 1, trackingNumber = "A"),
            cargoInfo(id = 2, trackingNumber = "B")
        )

        load()

        assertEquals(CargoDialog.None, viewModel.uiState.value.dialog)
    }

    // dismissDuplicateDialog seeded via a real duplicate load, not a test-only setter.
    @Test
    fun `dismissDuplicateDialog - clears duplicate state after a duplicate load`() = runTest(dispatcher) {
        stubCargoInfo(
            cargoInfo(id = 1, trackingNumber = "SAME"),
            cargoInfo(id = 2, trackingNumber = "SAME")
        )
        load()
        assertEquals(CargoDialog.Duplicates(listOf("SAME")), viewModel.uiState.value.dialog)

        viewModel.dismissDuplicateDialog()

        assertEquals(CargoDialog.None, viewModel.uiState.value.dialog)
    }

    // فیلترسازی جستجو دیگر بخشی از CargoViewModel نیست؛ به یک derived value محلی در CargoDetailsScreen.kt منتقل شد و اینجا قابل‌تست نیست

    // ===== updateCargoConfirmation (seeded via a real load) =====

    @Test
    fun `updateCargoConfirmation - marks matching cargo confirmed and stamps missing exit time-date`() = runTest(dispatcher) {
        stubCargoInfo(cargoInfo(id = 1, exitTime = null, exitDate = null))
        load()

        viewModel.updateCargoConfirmation(1)

        val updated = viewModel.uiState.value.cargoInfoList.first { it.id == 1 }
        assertEquals(com.atk.atk_cargo.domain.model.CargoConfirmStatus.CONFIRMED.wireValue, updated.confirm)
        assertTrue(updated.exitTime?.isNotBlank() == true)
        assertTrue(updated.exitDate?.isNotBlank() == true)
    }

    @Test
    fun `updateCargoConfirmation - preserves already-set exit time-date`() = runTest(dispatcher) {
        stubCargoInfo(cargoInfo(id = 1, exitTime = "12:00:00", exitDate = "1404-01-01"))
        load()

        viewModel.updateCargoConfirmation(1)

        val updated = viewModel.uiState.value.cargoInfoList.first { it.id == 1 }
        assertEquals("12:00:00", updated.exitTime)
        assertEquals("1404-01-01", updated.exitDate)
    }

    @Test
    fun `updateCargoConfirmation - unrelated id leaves list untouched`() = runTest(dispatcher) {
        stubCargoInfo(cargoInfo(id = 1))
        load()

        viewModel.updateCargoConfirmation(999)

        assertEquals(
            "no",
            viewModel.uiState.value.cargoInfoList.first { it.id == 1 }.confirm
        )
    }

    // ===== pure state, no seeding needed =====

    @Test
    fun `updateSelectedShips - replaces selection set`() {
        viewModel.updateSelectedShips(setOf("Ship-1", "Ship-2"))
        assertEquals(setOf("Ship-1", "Ship-2"), viewModel.uiState.value.selectedShipNames)
    }

    @Test
    fun `showMessage then dismissMessage - snackbar visibility toggles`() = runTest(dispatcher) {
        viewModel.showMessage("پیام تست", MessageType.SUCCESS)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.showAnimatedMessage.value)
        assertEquals("پیام تست", viewModel.resultMessage.value)

        viewModel.dismissMessage()
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.showAnimatedMessage.value)
    }
}
