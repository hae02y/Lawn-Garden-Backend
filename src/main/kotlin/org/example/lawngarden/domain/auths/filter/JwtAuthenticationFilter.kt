package org.example.lawngarden.domain.auths.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.lawngarden.common.exception.ApiErrorResponse
import org.example.lawngarden.domain.auths.token.TokenProvider
import org.example.lawngarden.domain.auths.service.CustomUserDetailsService
import org.example.lawngarden.domain.auths.token.TokenBlacklist
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val objectMapper: ObjectMapper,
    private val tokenProvider: TokenProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenBlacklist: TokenBlacklist
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)
        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            if (!tokenProvider.validateToken(token)) {
                unauthorized(response, "유효하지 않은 토큰입니다.")
                return
            }

            if (tokenBlacklist.isBlacklist(token)) {
                unauthorized(response, "로그아웃된 토큰입니다.")
                return
            }

            val username = tokenProvider.getUsernameFromToken(token)
            val userDetails = userDetailsService.loadUserByUsername(username)
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (_: UsernameNotFoundException) {
            unauthorized(response, "사용자를 찾을 수 없습니다.")
        } catch (_: Exception) {
            unauthorized(response, "인증 처리 중 오류가 발생했습니다.")
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        return if (bearer != null && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else null
    }

    private fun unauthorized(response: HttpServletResponse, message: String) {
        SecurityContextHolder.clearContext()
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        val error = ApiErrorResponse(errorCode = "UNAUTHORIZED", message = message)
        response.writer.write(objectMapper.writeValueAsString(error))
    }
}
