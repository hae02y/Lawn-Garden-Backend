package org.example.lawngarden.domain.notifications.dto

data class UserNotificationSettingResponseDto(
    val missionEnabled: Boolean,
    val streakRiskEnabled: Boolean,
    val levelUpEnabled: Boolean,
    val rewardEnabled: Boolean,
    val quietHoursEnabled: Boolean,
    val quietStartHour: Int,
    val quietEndHour: Int,
)
