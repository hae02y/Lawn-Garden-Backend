package org.example.lawngarden.domain.auths.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class OauthService(
    @Value("\${spring.security.oauth2.client.registration.github.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.github.client-secret}")
    private val clientSecret: String,
    @Value("\${spring.security.oauth2.client.registration.github.redirect-uri}")
    private val redirectUri: String,
    @Value("\${spring.security.oauth2.client.registration.github.scope}")
    private val scope: String,
) {
    private val authorizeUrl: String = "https://github.com/login/oauth/authorize"


    fun generateRedirectUrl(): String {
        val state = generateState()
        val url = buildString {
            append(authorizeUrl)
            append("?client_id=").append(encode(clientId))
            append("&redirect_uri=").append(encode(redirectUri))
            append("&scope=").append(encode(scope))
            append("&state=").append(encode(state))
        }

        return url
    }

    private fun generateState(): String = UUID.randomUUID().toString()
    private fun encode(v: String): String = URLEncoder.encode(v, StandardCharsets.UTF_8)
}