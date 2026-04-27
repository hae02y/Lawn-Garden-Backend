package org.example.lawngarden.domain.auths.token

import org.example.lawngarden.common.properties.JwtProperties
import org.example.lawngarden.domain.auths.enums.LoginType
import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.users.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenProviderTest {

    private val jwtProperties = JwtProperties(
        secret = "test-secret-key-for-jwt-token-provider-1234567890",
        accessTokenExpiration = 60_000,
        refreshTokenExpiration = 120_000,
    )

    private val tokenBlacklist = TokenBlacklist(jwtProperties)
    private val tokenProvider = TokenProvider(jwtProperties, tokenBlacklist)

    @Test
    fun createAndValidateAccessToken() {
        val user = testUser()
        val accessToken = tokenProvider.createAccessToken(user)

        assertTrue(tokenProvider.validateToken(accessToken))
        assertEquals(user.username, tokenProvider.getUsernameFromToken(accessToken))
    }

    @Test
    fun clearToken_blacklistsToken() {
        val user = testUser()
        val accessToken = tokenProvider.createAccessToken(user)

        tokenProvider.clearToken(user, accessToken)

        assertFalse(tokenProvider.validateToken("invalid-token"))
        assertTrue(tokenBlacklist.isBlacklist(accessToken))
    }

    private fun testUser(): User = User(
        id = 1L,
        username = "tester",
        email = "tester@example.com",
        password = "encoded",
        post = mutableListOf(),
        role = Role.USER,
        type = LoginType.NONE,
    )
}
