package org.example.lawngarden.domain.users.dto

import java.time.LocalDateTime

data class UserLevelHistoryResponseDto(
    val id: Long?,
    val previousLevel: Long,
    val previousLevelName: String,
    val newLevel: Long,
    val newLevelName: String,
    val postCount: Long,
    val changedAt: LocalDateTime?,
)
