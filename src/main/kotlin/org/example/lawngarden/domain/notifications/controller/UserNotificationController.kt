package org.example.lawngarden.domain.notifications.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.notifications.dto.NotificationReadResponseDto
import org.example.lawngarden.domain.notifications.dto.UserNotificationSettingRequestDto
import org.example.lawngarden.domain.notifications.dto.UserNotificationSettingResponseDto
import org.example.lawngarden.domain.notifications.dto.UserNotificationResponseDto
import org.example.lawngarden.domain.notifications.service.UserNotificationService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "알림 API")
class UserNotificationController(
    private val notificationService: UserNotificationService,
) {
    @GetMapping("/me")
    @Operation(summary = "내 알림 조회")
    fun getMyNotifications(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
        @RequestParam("unreadOnly", defaultValue = "false") unreadOnly: Boolean,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): ResponseEntity<Page<UserNotificationResponseDto>> {
        return ResponseEntity.ok(notificationService.getMyNotifications(userDetails.user, unreadOnly, page, size))
    }

    @PostMapping("/me/refresh")
    @Operation(summary = "내 알림 재생성")
    fun refreshMyNotifications(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<Page<UserNotificationResponseDto>> {
        return ResponseEntity.ok(notificationService.refreshUserNotifications(userDetails.user))
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리")
    fun markNotificationRead(
        @PathVariable notificationId: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<NotificationReadResponseDto> {
        return ResponseEntity.ok(notificationService.markAsRead(userDetails.user, notificationId))
    }

    @PostMapping("/me/read-all")
    @Operation(summary = "알림 전체 읽음 처리")
    fun markAllNotificationsRead(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<Map<String, Int>> {
        val updatedCount = notificationService.markAllAsRead(userDetails.user)
        return ResponseEntity.ok(mapOf("updatedCount" to updatedCount))
    }

    @GetMapping("/me/settings")
    @Operation(summary = "알림 설정 조회")
    fun getNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<UserNotificationSettingResponseDto> {
        return ResponseEntity.ok(notificationService.getNotificationSettings(userDetails.user))
    }

    @PutMapping("/me/settings")
    @Operation(summary = "알림 설정 변경")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
        @RequestBody request: UserNotificationSettingRequestDto,
    ): ResponseEntity<UserNotificationSettingResponseDto> {
        return ResponseEntity.ok(notificationService.updateNotificationSettings(userDetails.user, request))
    }
}
