package org.example.lawngarden.domain.auths.security.ouath2

import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class Oauth2Service(
    private final val userRepository: UserRepository,
) {
    fun findUser(user : OAuth2User) : OAuth2User{

        val attribute = user.getAttribute<String>("login")
        val attribute1 = user.getAttribute<String>("name")
        val attribute2 = user.getAttribute<String>("email")
        println(attribute)
        println(attribute1)
        println(attribute2)
        return user
    }
}