package org.example.lawngarden.domain.auths.service

import org.example.lawngarden.domain.auths.dto.CustomOAuth2User
import org.example.lawngarden.domain.auths.enums.LoginType
import org.example.lawngarden.domain.mapper.toUser
import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOauth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes
        val userName = attributes["login"]?.toString()?.trim().orEmpty()
        require(userName.isNotBlank()) { "OAuth 사용자 정보가 올바르지 않습니다." }

        val email = attributes["email"]?.toString()?.trim().takeUnless { it.isNullOrBlank() }
            ?: "$userName@users.noreply.github.com"

        val user: User = userRepository.findByEmail(email)
            ?: saveOauth2User(RegisterRequestDto(userName, null, email, LoginType.GITHUB))
        return CustomOAuth2User(user, attributes) // 3. 성공한 유저 정보를 SuccessHandler로 넘김
    }


    private fun saveOauth2User(registerRequestDto: RegisterRequestDto): User =
        userRepository.save(registerRequestDto.toUser())
}
