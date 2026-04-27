package org.example.lawngarden.domain.auths.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.auths.dto.LoginRequest
import org.example.lawngarden.domain.auths.dto.LoginResponse
import org.example.lawngarden.domain.auths.dto.RefreshTokenRequest
import org.example.lawngarden.domain.auths.token.TokenProvider
import org.example.lawngarden.domain.mapper.toUserDetailResponseDto
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 API")
class AuthController(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
) {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    fun login(@RequestBody loginRequest : LoginRequest): ResponseEntity<LoginResponse> {
        val authentication : Authentication = authentication(loginRequest.username, loginRequest.password)
        val userDetails = authentication.principal as UserDetailsImpl
        val user : User = userDetails.user

        val accessToken : String = tokenProvider.createAccessToken(user)
        val refreshToken : String = tokenProvider.createRefreshToken(user)

        val loginResponse = LoginResponse(
            BEARER_PREFIX + accessToken,
            BEARER_PREFIX + refreshToken,
            user.toUserDetailResponseDto(),
        )
        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<LoginResponse> {
        val refreshToken = normalizeToken(request.refreshToken)

        if (!tokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")
        }

        val username = tokenProvider.getUsernameFromToken(refreshToken)
        val user = userRepository.findByUsername(username)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다.")

        val newAccessToken = tokenProvider.createAccessToken(user)
        val newRefreshToken = tokenProvider.createRefreshToken(user)

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = BEARER_PREFIX + newAccessToken,
                refreshToken = BEARER_PREFIX + newRefreshToken,
                user = user.toUserDetailResponseDto(),
            )
        )
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    fun logout(@AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
               @RequestHeader("Authorization") token : String): ResponseEntity<Void> {
        val user: User = userDetailsImpl.user
        val accessToken = normalizeToken(token)
        tokenProvider.clearToken(user, accessToken)

        return ResponseEntity.noContent().build()
    }


    private fun authentication(username:String, password:String): Authentication {
        val token = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(token)
    }

    private fun normalizeToken(token: String): String {
        val normalized = token.removePrefix(BEARER_PREFIX).trim()
        if (normalized.isBlank()) throw IllegalArgumentException("토큰이 비어있습니다.")
        return normalized
    }
}
