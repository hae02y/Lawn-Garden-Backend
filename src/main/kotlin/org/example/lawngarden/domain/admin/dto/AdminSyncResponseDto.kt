package org.example.lawngarden.domain.admin.dto

data class AdminSyncResponseDto(
    val success: Boolean,
    val message: String,
    val affectedCount: Int,
)
