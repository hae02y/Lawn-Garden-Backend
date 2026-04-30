package org.example.lawngarden.domain.achievements.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.achievements.dto.UserAchievementResponseDto
import org.example.lawngarden.domain.achievements.service.UserAchievementService
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Achievements", description = "업적 API")
class UserAchievementController(
    private val userAchievementService: UserAchievementService,
) {
    @GetMapping("/me")
    @Operation(summary = "내 업적 조회")
    fun getMyAchievements(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<List<UserAchievementResponseDto>> {
        return ResponseEntity.ok(userAchievementService.getMyAchievements(userDetails.user))
    }

    @PostMapping("/me/refresh")
    @Operation(summary = "내 업적 재계산")
    fun refreshMyAchievements(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<List<UserAchievementResponseDto>> {
        return ResponseEntity.ok(userAchievementService.refreshMyAchievements(userDetails.user))
    }
}
