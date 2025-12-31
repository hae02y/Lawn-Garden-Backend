package org.example.lawngarden.domain.auths.controller

import org.example.lawngarden.domain.auths.service.OauthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/api/v1/oauth2")
class OauthController(
    private val oauthService: OauthService
) {

    @GetMapping("/login/authorize")
    fun oauthLoginRedirect() : String {
        val generatedURL = oauthService.generateRedirectUrl()
        return "redirect:$generatedURL"
    }

    @GetMapping("/login/code/github")
    fun oauthGithubLogin(@RequestParam code:String) : ResponseEntity<Any> {
        oauthService
        return ResponseEntity.ok<Any>(HashMap<String, String>().put("accessToken", "sadjflksjflsfjlksjdflasjflaskjdflk"))
    }


}