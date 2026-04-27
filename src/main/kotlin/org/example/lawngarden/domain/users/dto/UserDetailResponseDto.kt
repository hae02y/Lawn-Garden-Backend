package org.example.lawngarden.domain.users.dto

data class UserDetailResponseDto(
    val id: Long?,
    val username: String,
    val email: String,
    val level: Long,
    val levelName: String,
)
