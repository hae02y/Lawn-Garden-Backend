package org.example.lawngarden.domain.notifications.dto

import org.example.lawngarden.domain.notifications.enums.NotificationSeverity
import java.time.LocalDate
import java.time.LocalDateTime

data class UserNotificationResponseDto(
    val id: Long?,
    val title: String,
    val message: String,
    val severity: NotificationSeverity,
    val code: String,
    val referenceDate: LocalDate?,
    val isRead: Boolean,
    val createdAt: LocalDateTime?,
)
