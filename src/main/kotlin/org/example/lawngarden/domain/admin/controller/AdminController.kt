package org.example.lawngarden.domain.admin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.admin.dto.AdminSyncResponseDto
import org.example.lawngarden.domain.admin.service.AdminService
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.geeknews.dto.GeekNewsSyncLogResponseDto
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "운영자 기능 API")
class AdminController(
    private val adminService: AdminService,
) {
    @PostMapping("/geeknews/sync")
    @Operation(summary = "GeekNews 수동 동기화")
    fun syncGeekNews(
        @RequestParam("limit", defaultValue = "50") limit: Int,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<AdminSyncResponseDto> {
        return ResponseEntity.ok(adminService.syncGeekNews(limit, userDetails.user))
    }

    @GetMapping("/geeknews/sync-logs")
    @Operation(summary = "GeekNews 동기화 로그 조회")
    fun getGeekNewsSyncLogs(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<Page<GeekNewsSyncLogResponseDto>> {
        return ResponseEntity.ok(adminService.getGeekNewsSyncLogs(page, size, userDetails.user))
    }

    @PostMapping("/users/levels/sync")
    @Operation(summary = "사용자 레벨 일괄 동기화")
    fun syncAllUserLevels(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<AdminSyncResponseDto> {
        return ResponseEntity.ok(adminService.syncUserLevels(userDetails.user))
    }
}
