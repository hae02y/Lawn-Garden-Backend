package org.example.lawngarden.domain.social.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.social.dto.CheerRequestDto
import org.example.lawngarden.domain.social.dto.CheerStatusResponseDto
import org.example.lawngarden.domain.social.dto.WeeklyLeaderboardItemDto
import org.example.lawngarden.domain.social.service.SocialService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/social")
@Tag(name = "Social", description = "소셜 기능 API")
class SocialController(
    private val socialService: SocialService,
) {
    @PostMapping("/cheer/{targetUserId}")
    @Operation(summary = "사용자 응원 보내기")
    fun cheerUser(
        @PathVariable targetUserId: Long,
        @RequestBody request: CheerRequestDto,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<CheerStatusResponseDto> {
        return ResponseEntity.ok(socialService.cheerUser(userDetails.user, targetUserId, request.type))
    }

    @GetMapping("/cheer/{targetUserId}/status")
    @Operation(summary = "사용자 응원 가능 상태 조회")
    fun getCheerStatus(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<CheerStatusResponseDto> {
        return ResponseEntity.ok(socialService.getCheerStatus(userDetails.user, targetUserId))
    }

    @GetMapping("/leaderboard/weekly")
    @Operation(summary = "주간 랭킹 조회")
    fun getWeeklyLeaderboard(
        @RequestParam("limit", defaultValue = "10") limit: Int,
    ): ResponseEntity<List<WeeklyLeaderboardItemDto>> {
        return ResponseEntity.ok(socialService.getWeeklyLeaderboard(limit))
    }
}
