package org.example.lawngarden.domain.achievements.dto

import org.example.lawngarden.domain.achievements.enums.AchievementCode
import java.time.LocalDateTime

data class UserAchievementResponseDto(
    val code: AchievementCode,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAt: LocalDateTime?,
)
