package com.atk.atk_cargo.api

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// تست بدون دستگاه برای TokenAuthenticator.authenticate با Response/Request دستی‌ساز؛ TokenRefresher با mockkObject جایگزین می‌شود تا HTTP واقعی درگیر نشود
class TokenAuthenticatorTest {
    private val request = Request.Builder().url("https://atk-nk.ir/Cargo/x").build()

    private fun responseWithCode(
        code: Int,
        body: String = "",
        headerSessionToken: String? = "old-token",
        prior: Response? = null
    ): Response {
        val reqBuilder = request.newBuilder()
        headerSessionToken?.let { reqBuilder.header("X-Session-Token", it) }
        return Response.Builder()
            .request(reqBuilder.build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body(body.toResponseBody(null))
            .apply { if (prior != null) priorResponse(prior) }
            .build()
    }

    @Before
    fun setUp() {
        AuthSession.clear()
        mockkObject(TokenRefresher)
    }

    @After
    fun tearDown() {
        AuthSession.clear()
        unmockkObject(TokenRefresher)
    }

    @Test
    fun `کد غیر ۴۰۱ - بدون تلاش برای رفرش`() {
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", mockk())
        val response = responseWithCode(500)

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        coVerify(exactly = 0) { TokenRefresher.refresh(any(), any()) }
    }

    @Test
    fun `۴۰۱ بدون code=access_token_expired - نادیده گرفته می‌شود`() {
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", mockk())
        val response = responseWithCode(401, body = """{"error":"invalid_session"}""")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        coVerify(exactly = 0) { TokenRefresher.refresh(any(), any()) }
    }

    @Test
    fun `۴۰۱ با access_token_expired - رفرش موفق و retry با هدر جدید`() {
        val tokenStore = mockk<TokenStore>()
        coEvery { TokenRefresher.refresh(any(), any()) } returns "brand-new-token"
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", tokenStore)
        val response = responseWithCode(401, body = """{"code":"access_token_expired"}""", headerSessionToken = "old-token")

        val result = authenticator.authenticate(null, response)

        assertEquals("brand-new-token", result?.header("X-Session-Token"))
        coVerify(exactly = 1) { TokenRefresher.refresh("https://atk-nk.ir/", tokenStore) }
    }

    @Test
    fun `رفرش ناموفق - null برمی‌گرداند (کلاینت به login می‌رود)`() {
        val tokenStore = mockk<TokenStore>()
        coEvery { TokenRefresher.refresh(any(), any()) } returns null
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", tokenStore)
        val response = responseWithCode(401, body = """{"code":"access_token_expired"}""")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }

    @Test
    fun `دومین شکست پیاپی روی همان درخواست - از حلقه‌ی بی‌نهایت جلوگیری می‌شود`() {
        val tokenStore = mockk<TokenStore>()
        coEvery { TokenRefresher.refresh(any(), any()) } returns "should-not-be-used"
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", tokenStore)
        val first = responseWithCode(401, body = """{"code":"access_token_expired"}""")
        val second = responseWithCode(401, body = """{"code":"access_token_expired"}""", prior = first)

        val result = authenticator.authenticate(null, second)

        assertNull(result)
        coVerify(exactly = 0) { TokenRefresher.refresh(any(), any()) }
    }

    @Test
    fun `درخواست موازی قبلاً همین توکن را رفرش کرده - دوباره رفرش نمی‌کند`() {
        // شبیه‌سازی race: درخواست موازی دیگری قبلاً رفرش را انجام داده؛ باید همان توکن تازه برگردد بدون فراخوانی دوباره‌ی TokenRefresher
        AuthSession.sessionToken = "already-refreshed-by-another-request"
        val tokenStore = mockk<TokenStore>()
        val authenticator = TokenAuthenticator("https://atk-nk.ir/", tokenStore)
        val response = responseWithCode(401, body = """{"code":"access_token_expired"}""", headerSessionToken = "old-token")

        val result = authenticator.authenticate(null, response)

        assertEquals("already-refreshed-by-another-request", result?.header("X-Session-Token"))
        coVerify(exactly = 0) { TokenRefresher.refresh(any(), any()) }
    }
}
