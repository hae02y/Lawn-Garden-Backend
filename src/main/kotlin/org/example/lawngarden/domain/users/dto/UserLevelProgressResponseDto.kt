package org.example.lawngarden.domain.users.dto

data class UserLevelProgressResponseDto(
    val currentLevel: Long,
    val currentLevelName: String,
    val currentBadge: String,
    val postCount: Long,
    val nextLevel: Long?,
    val nextLevelName: String?,
    val nextLevelMinPostCount: Long?,
    val remainingPostCount: Long,
)
