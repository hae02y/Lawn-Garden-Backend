package org.example.lawngarden.domain.social.dto

data class WeeklyLeaderboardItemDto(
    val userId: Long,
    val username: String,
    val weeklyPostCount: Long,
    val streakDays: Int,
    val growthRate: Double,
)
