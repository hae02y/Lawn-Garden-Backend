package org.example.lawngarden.domain.notifications.repository

import org.example.lawngarden.domain.notifications.entity.UserNotification
import org.springframework.data.jpa.repository.JpaRepository

interface UserNotificationRepository : JpaRepository<UserNotification, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<UserNotification>
    fun findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId: Long): List<UserNotification>
    fun existsByUserIdAndCodeAndReferenceDate(userId: Long, code: String, referenceDate: java.time.LocalDate?): Boolean
}
