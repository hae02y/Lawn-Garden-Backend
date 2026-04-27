package org.example.lawngarden.domain.push.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.push.dto.MailStatusResponseDto
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.service.MailService
import org.example.lawngarden.domain.users.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/mails")
@Tag(name = "Mails", description = "메일 상태 API")
class MailController(
    private val mailService: MailService,
) {

    @GetMapping("/me")
    @Operation(summary = "내 메일 상태 조회")
    fun getMyMailStatus(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<MailStatusResponseDto> {
        val user: User = userDetails.user
        val status = mailService.getMailStatus(user)
        return ResponseEntity.ok(MailStatusResponseDto(status))
    }

    @PostMapping()
    @Operation(summary = "메일 상태 변경")
    fun changeMailStatus(
        @RequestBody mailStatus: MailStatus,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<MailStatusResponseDto> {
        val user: User = userDetails.user
        val changedStatus = mailService.changeMailStatus(mailStatus, user)
        return ResponseEntity.ok(MailStatusResponseDto(changedStatus))
    }

}
