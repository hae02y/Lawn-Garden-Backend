package org.example.lawngarden.domain.auths.service

import org.example.lawngarden.domain.auths.dto.CustomOAuth2User
import org.example.lawngarden.domain.mapper.toUser
import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.repository.UserRepository
import org.example.lawngarden.domain.users.service.UserService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOauth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User? {

        val oAuth2User = super.loadUser(userRequest) // 1. GitHub 정보 획득

        val attributes = oAuth2User.attributes
        val email = attributes["email"].toString()
        val githubId = attributes["id"].toString()

        val user : User = (userRepository.findByEmail((email))
            ?: this.saveOath2User(registerRequestDto = RegisterRequestDto(githubId, email, githubId)))
        return CustomOAuth2User(user, attributes) // 3. 성공한 유저 정보를 SuccessHandler로 넘김
    }


    private fun saveOath2User(registerRequestDto: RegisterRequestDto) : User {
        val user : User = userRepository.save(registerRequestDto.toUser())
        return user
    }
}