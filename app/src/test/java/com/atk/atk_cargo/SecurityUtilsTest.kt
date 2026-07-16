package com.atk.atk_cargo

import com.atk.atk_cargo.utils.hashPassword
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun hashPassword_withValidInput_returnsCorrectSha256Hash() {
        val rawPassword = "admin"
        // SHA-256 hash for "admin"
        val expectedHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"
        
        val actualHash = hashPassword(rawPassword)
        
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun hashPassword_withEmptyInput_returnsCorrectEmptyHash() {
        val rawPassword = ""
        // SHA-256 hash for empty string
        val expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        
        val actualHash = hashPassword(rawPassword)
        
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun hashPassword_differentInputs_produceDifferentHashes() {
        val hash1 = hashPassword("pass1")
        val hash2 = hashPassword("pass2")
        
        assertNotEquals(hash1, hash2)
    }
}
