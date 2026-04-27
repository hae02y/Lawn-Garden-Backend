package org.example.lawngarden.domain.stats.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.stats.service.StatsService
import org.example.lawngarden.domain.users.dto.UserStatsResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "Stats", description = "통계 API")
class StatsController(
    private val statsService : StatsService,
) {



    @GetMapping("/weekly")
    @Operation(summary = "주간 통계 조회")
    fun getWeeklyStats() : ResponseEntity<List<UserStatsResponseDto>> {
        val weeklyStats = statsService.getWeeklyStats()
        return ResponseEntity.ok(weeklyStats)
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 통계 조회")
    fun getTodayStats(): ResponseEntity<List<UserStatsResponseDto>> {
        val todayStats = statsService.getTodayStats()
        return ResponseEntity.ok(todayStats)
    }
}
