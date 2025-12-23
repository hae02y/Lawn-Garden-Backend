package org.example.lawngarden.domain.push.controller

import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.service.MailService
import org.example.lawngarden.domain.users.entity.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/mails")
class MailController(
    private val mailService: MailService,
) {

    @PostMapping()
    fun changeMailStatus(@RequestBody mailStatus: MailStatus,
                         @AuthenticationPrincipal userDetails: UserDetailsImpl,
                         ) {
        val user : User = userDetails.user
        mailService.changeMailStatus(mailStatus, user);
    }

}