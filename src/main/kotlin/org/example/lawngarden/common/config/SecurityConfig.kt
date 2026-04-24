package org.example.lawngarden.common.config

import org.example.lawngarden.domain.auths.dto.CustomOAuth2User
import org.example.lawngarden.domain.auths.filter.JwtAuthenticationFilter
import org.example.lawngarden.domain.auths.service.CustomOauth2UserService
import org.example.lawngarden.domain.auths.token.TokenProvider
import org.example.lawngarden.domain.users.entity.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOauth2UserService: CustomOauth2UserService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val tokenProvider: TokenProvider,
    @Value("\${app.front-callback}")
    private val frontCallback: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/api/v1/geeknews/**").permitAll()
                it.requestMatchers(
                    "/",
                    "/api/v1/users/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/logout",
                    "/api/v1/oauth/**",
                    "/api/v1/oauth2/**",
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v1/mails/**",
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login { oauth ->
                oauth.userInfoEndpoint { it.userService(customOauth2UserService) }
                    .successHandler(customOauth2SuccessHandler())
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        val frontOrigin = runCatching {
            val uri = URI(frontCallback)
            "${uri.scheme}://${uri.authority}"
        }.getOrNull()

        config.allowedOrigins = listOfNotNull(
            frontOrigin,
            "http://localhost:3000",
            "http://localhost:5173",
            "https://lawngarden.netlify.app",
            "https://my-lawn.netlify.app",
        ).distinct()
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
            val userId = user.id

            val redirectUrl = UriComponentsBuilder.fromUriString(frontCallback)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("username", username)
                .queryParam("userId", userId)
                .build()
                .encode()
                .toUriString()
            response.sendRedirect(redirectUrl)
        }
    }
}
