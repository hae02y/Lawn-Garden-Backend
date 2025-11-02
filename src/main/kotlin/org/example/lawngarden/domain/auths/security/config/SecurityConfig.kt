package org.example.lawngarden.domain.auths.security.config

import org.example.lawngarden.domain.auths.filter.JwtAuthenticationFilter
import org.example.lawngarden.domain.auths.security.ouath2.Oauth2Service
import org.example.lawngarden.domain.auths.service.InMemoryCodeStore
import org.hibernate.internal.util.collections.Stack
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ClientRegistrations
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val codeStore: InMemoryCodeStore,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val oath2Service : Oauth2Service,

    @param:Value("\${app.front-callback}")
    private val callbackUri: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
            it.requestMatchers(
                "/api/v1/users/register",
                "/api/v1/auth/login",
                "/swagger-ui/**",
                "v3/api-docs/**",
            ).permitAll()
            it.anyRequest().authenticated() }
            .oauth2Login {oauth -> oauth.successHandler(customOauth2SuccessHandler())}
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
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
    fun customOauth2SuccessHandler() : AuthenticationSuccessHandler {
        return AuthenticationSuccessHandler {
            _, response, authentication ->
            val user = authentication.principal as OAuth2User

            oath2Service.findUser(user);

            val githubId = user.getAttribute<Any>("id").toString()
            val login = user.getAttribute<String>("login") ?: "unknown"
            val code = codeStore.issue(githubId, login)
            response.sendRedirect("$callbackUri?code=$code")
        }
    }

}
