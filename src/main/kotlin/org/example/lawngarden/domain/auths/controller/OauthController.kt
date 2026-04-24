package org.example.lawngarden.domain.auths.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth", description = "OAuth 로그인 API")
class OauthController {

    @GetMapping("/login/authorize")
    @Operation(summary = "GitHub OAuth 시작")
    fun oauthLoginRedirect(): ResponseEntity<Void> {
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("/oauth2/authorization/github"))
            .build()
    }
}
