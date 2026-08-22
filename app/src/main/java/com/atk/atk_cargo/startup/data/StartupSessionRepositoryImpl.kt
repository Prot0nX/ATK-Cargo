package com.atk.atk_cargo.startup.data

import com.atk.atk_cargo.api.ApiServiceV2
import com.atk.atk_cargo.api.SessionCheckRequest
import java.io.IOException

class StartupSessionRepositoryImpl(
    private val apiServiceV2: ApiServiceV2
) : StartupSessionRepository {

    override suspend fun checkSession(
        username: String,
        deviceId: String,
        sessionToken: String?
    ): SessionCheckOutcome {
        return try {
            val response = apiServiceV2.checkSession(SessionCheckRequest(username, deviceId, sessionToken))
            when {
                !response.isSuccessful && response.code() >= 500 -> SessionCheckOutcome.Unreachable
                response.isSuccessful && response.body()?.success == true -> SessionCheckOutcome.Valid
                else -> SessionCheckOutcome.Invalid
            }
        } catch (e: IOException) {
            SessionCheckOutcome.Unreachable
        } catch (_: Exception) {
            SessionCheckOutcome.Invalid
        }
    }
}
