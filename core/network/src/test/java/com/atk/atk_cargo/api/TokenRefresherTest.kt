package com.atk.atk_cargo.api

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * تست بدون دستگاه/امولاتور برای منطق واقعی POST /auth/refresh
 * (DEEP_CODE_AUDIT.md #Phase5.7 — «رفتار runtime شبکه تست نشده»). MockWebServer
 * یک سرور HTTP واقعی روی JVM است، پس این تست‌ها مسیر HTTP واقعی (نه فقط
 * منطق پارس) را پوشش می‌دهند؛ فقط چیزی که واقعاً به دستگاه نیاز دارد
 * (Certificate Pinning روی TLS واقعی) خارج از این تست‌ها می‌ماند.
 */
class TokenRefresherTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun mockTokenStore(
        username: String = "ali",
        deviceId: String = "device-1",
        refreshToken: String = "old-refresh-token"
    ): TokenStore {
        val store = mockk<TokenStore>(relaxUnitFun = true)
        coEvery { store.getUsername() } returns username
        coEvery { store.getDeviceId() } returns deviceId
        coEvery { store.getRefreshToken() } returns refreshToken
        return store
    }

    @Test
    fun `refresh موفق - توکن جدید را برمی‌گرداند و در tokenStore ذخیره می‌کند`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"session_token":"new-access","refresh_token":"new-refresh"}""")
        )
        val tokenStore = mockTokenStore()

        val result = TokenRefresher.refresh(server.url("/").toString(), tokenStore)

        assertEquals("new-access", result)
        coVerify(exactly = 1) { tokenStore.saveRefreshedTokens("new-access", "new-refresh") }

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        // مسیر تمیز /api/v2/auth/refresh روی این هاست کار نمی‌کند؛ کد عمداً از
        // فرمت query-string استفاده می‌کند (کامنت TokenRefresher.kt) — این تست
        // همان قرارداد را تثبیت می‌کند تا رگرسیون بی‌صدا نشود.
        assertEquals(true, recorded.path?.contains("route=auth/refresh"))
        val body = recorded.body.readUtf8()
        assertEquals(true, body.contains("username=ali"))
        assertEquals(true, body.contains("deviceId=device-1"))
        assertEquals(true, body.contains("refreshToken=old-refresh-token"))
    }

    @Test
    fun `refresh token رد شده با ۴۰۱ - نشست محلی پاک می‌شود و null برمی‌گردد`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"success":false}"""))
        val tokenStore = mockTokenStore()

        val result = TokenRefresher.refresh(server.url("/").toString(), tokenStore)

        assertNull(result)
        coVerify(exactly = 1) { tokenStore.clearCredentials() }
        coVerify(exactly = 0) { tokenStore.saveRefreshedTokens(any(), any()) }
    }

    @Test
    fun `پاسخ success=false بدون کد ۴۰۱ - null برمی‌گردد و نشست پاک نمی‌شود`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":false}"""))
        val tokenStore = mockTokenStore()

        val result = TokenRefresher.refresh(server.url("/").toString(), tokenStore)

        assertNull(result)
        coVerify(exactly = 0) { tokenStore.clearCredentials() }
        coVerify(exactly = 0) { tokenStore.saveRefreshedTokens(any(), any()) }
    }

    @Test
    fun `بدنه‌ی JSON نامعتبر - استثنا نمی‌اندازد، فقط null برمی‌گرداند`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("این یک JSON معتبر نیست"))
        val tokenStore = mockTokenStore()

        val result = TokenRefresher.refresh(server.url("/").toString(), tokenStore)

        assertNull(result)
    }

    @Test
    fun `اعتبار محلی ناقص - بدون هیچ درخواست HTTP فوراً null برمی‌گرداند`() = runTest {
        val tokenStore = mockTokenStore(refreshToken = "")

        val result = TokenRefresher.refresh(server.url("/").toString(), tokenStore)

        assertNull(result)
        assertEquals(0, server.requestCount)
    }
}
