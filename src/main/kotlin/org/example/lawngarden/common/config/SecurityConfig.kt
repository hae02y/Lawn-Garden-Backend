package org.example.lawngarden.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.lawngarden.domain.auths.dto.CustomOAuth2User
import org.example.lawngarden.domain.auths.dto.LoginResponse
import org.example.lawngarden.domain.auths.filter.JwtAuthenticationFilter
import org.example.lawngarden.domain.auths.service.CustomOauth2UserService
import org.example.lawngarden.domain.auths.service.InMemoryCodeStore
import org.example.lawngarden.domain.auths.token.TokenProvider
import org.example.lawngarden.domain.mapper.toUserDetailResponseDto
import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import org.example.lawngarden.domain.users.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOauth2UserService: CustomOauth2UserService,
    private val codeStore: InMemoryCodeStore,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val tokenProvider: TokenProvider,
    private val objectMapper: ObjectMapper,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
            it.requestMatchers(
                "/",
                "/api/v1/users/register",
                "/api/v1/auth/login",
                "/swagger-ui/**",
                "v3/api-docs/**",
                "/api/v1/mails/**",
//                "/api/v1/oauth2/login/code/**"
            ).permitAll()
            it.anyRequest().authenticated() }
            .oauth2Login {oauth ->
                oauth.userInfoEndpoint { it.userService(customOauth2UserService) }
                    .successHandler(customOauth2SuccessHandler())}
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration) : AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://lawngarden.netlify.app"
            )
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = false

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun customOauth2SuccessHandler(): AuthenticationSuccessHandler {
        return AuthenticationSuccessHandler { _, response, authentication ->
            val oAuth2User = authentication.principal as CustomOAuth2User
            val user: User = oAuth2User.user
            val accessToken = tokenProvider.createAccessToken(user)
            val refreshToken = tokenProvider.createRefreshToken(user)
            val username = user.username
            // 프론트로 리다이렉트 (쿼리로 전달)
            val redirectUrl = "http://localhost:5173/oauth/github" +
                    "?accessToken=$accessToken" +
                    "&refreshToken=$refreshToken" +
                    "&username=$username"
            response.sendRedirect(redirectUrl)
        }
    }

}