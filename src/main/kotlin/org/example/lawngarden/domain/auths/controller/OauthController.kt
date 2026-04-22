package org.example.lawngarden.domain.auths.controller

import org.example.lawngarden.domain.auths.service.OauthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/oauth2")
class OauthController(
    private val oauthService: OauthService,
) {

    @GetMapping("/login/authorize")
    fun oauthLoginRedirect(): ResponseEntity<Void> {
        val generatedURL = oauthService.generateRedirectUrl()
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(generatedURL))
            .build()
    }

    @GetMapping("/login/code/github")
    fun oauthGithubLogin(@RequestParam code: String): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf("accessToken" to "sadjflksjflsfjlksjdflasjflaskjdflk")
        )
    }
}
