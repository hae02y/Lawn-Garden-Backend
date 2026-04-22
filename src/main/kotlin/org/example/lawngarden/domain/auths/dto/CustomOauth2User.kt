package org.example.lawngarden.domain.auths.dto

import org.example.lawngarden.domain.users.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User


class CustomOAuth2User(
    val user: User, // 우리 엔티티
    private val attributes: Map<String, Any> // GitHub이 준 정보
) : OAuth2User {

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority?>? =
        mutableListOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getName(): String = user.id.toString()
}