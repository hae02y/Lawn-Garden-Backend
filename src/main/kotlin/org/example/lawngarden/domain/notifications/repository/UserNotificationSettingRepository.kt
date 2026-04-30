package org.example.lawngarden.domain.notifications.repository

import org.example.lawngarden.domain.notifications.entity.UserNotificationSetting
import org.springframework.data.jpa.repository.JpaRepository

interface UserNotificationSettingRepository : JpaRepository<UserNotificationSetting, Long> {
    fun findByUserId(userId: Long): UserNotificationSetting?
}
