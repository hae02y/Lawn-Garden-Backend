package org.example.lawngarden.domain.users.dto

data class UserStatsResponseDto(
    val id: Long?,
    val username: String,
    val email: String,
    val commitCount: Long
)
