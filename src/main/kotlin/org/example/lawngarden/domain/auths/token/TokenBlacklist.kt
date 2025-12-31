package org.example.lawngarden.domain.auths.token

import com.github.benmanes.caffeine.cache.Caffeine
import org.example.lawngarden.common.properties.JwtProperties
import org.example.lawngarden.domain.users.entity.User
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class TokenBlacklist(
    private val tokenProperties: JwtProperties
) {

    private val blacklist = Caffeine.newBuilder()
        .expireAfterWrite(tokenProperties.accessTokenExpiration,TimeUnit.MILLISECONDS) //토큰 만료 기간 삽입
        .maximumSize(10_000)
        .build<String, String>()

    fun add(user: User, token: String) {
        blacklist.put(token,user.username)
    }

    fun isBlacklist(token:String) : Boolean {
        return blacklist.getIfPresent(token) != null
    }
}