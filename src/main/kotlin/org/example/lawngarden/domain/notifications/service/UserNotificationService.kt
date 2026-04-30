package org.example.lawngarden.domain.notifications.service

import org.example.lawngarden.domain.notifications.dto.NotificationReadResponseDto
import org.example.lawngarden.domain.notifications.dto.UserNotificationResponseDto
import org.example.lawngarden.domain.notifications.entity.UserNotification
import org.example.lawngarden.domain.notifications.enums.NotificationSeverity
import org.example.lawngarden.domain.notifications.repository.UserNotificationRepository
import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.enums.UserLevel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.NoSuchElementException

@Service
class UserNotificationService(
    private val notificationRepository: UserNotificationRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun refreshUserNotifications(user: User): List<UserNotificationResponseDto> {
        val userId = user.id ?: return emptyList()
        val today = LocalDate.now()

        val postDates = postRepository.findDistinctCreatedDatesByUserId(userId)
        val postDateSet = postDates.toSet()
        val postCount = postRepository.countByUserId(userId)
        val currentLevel = UserLevel.fromPostCount(postCount)
        val nextLevel = UserLevel.nextLevel(currentLevel)
        val streakDays = calculateStreak(postDateSet, today)
        val hasTodayPost = postDateSet.contains(today)

        if (!hasTodayPost) {
            createIfAbsent(
                user = user,
                title = "오늘 미션 남음",
                message = "오늘 물주기를 완료하면 연속 기록을 유지할 수 있어요.",
                severity = NotificationSeverity.WARN,
                code = "TODAY_MISSION_MISSING",
                referenceDate = today,
            )
        }

        if (!hasTodayPost && streakDays > 0) {
            createIfAbsent(
                user = user,
                title = "연속 인증 주의",
                message = "오늘 인증을 놓치면 ${streakDays}일 연속 기록이 끊겨요.",
                severity = NotificationSeverity.WARN,
                code = "STREAK_AT_RISK",
                referenceDate = today,
            )
        }

        if (nextLevel != null) {
            val remaining = (nextLevel.minPostCount - postCount).coerceAtLeast(0)
            if (remaining in 1..3) {
                createIfAbsent(
                    user = user,
                    title = "다음 레벨 임박",
                    message = "다음 레벨(${nextLevel.displayName})까지 ${remaining}회 남았어요.",
                    severity = NotificationSeverity.INFO,
                    code = "NEAR_LEVEL_UP",
                    referenceDate = today,
                )
            }
        }

        if (streakDays >= 7) {
            val segment = when {
                streakDays >= 30 -> 30
                streakDays >= 14 -> 14
                else -> 7
            }
            createIfAbsent(
                user = user,
                title = "연속 보상 구간 도달",
                message = "${streakDays}일 연속 달성! 보상 배지를 확인해보세요.",
                severity = NotificationSeverity.SUCCESS,
                code = "STREAK_REWARD_$segment",
                referenceDate = today,
            )
        }

        return getMyNotifications(user)
    }

    @Transactional(readOnly = true)
    fun getMyNotifications(user: User): List<UserNotificationResponseDto> {
        val userId = user.id ?: return emptyList()
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { it.toDto() }
    }

    @Transactional
    fun markAsRead(user: User, notificationId: Long): NotificationReadResponseDto {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found. id=$notificationId") }
        if (notification.user.id != user.id) {
            throw IllegalArgumentException("다른 사용자의 알림은 읽음 처리할 수 없습니다.")
        }
        notification.markRead()
        return NotificationReadResponseDto(id = notificationId, isRead = true)
    }

    @Transactional
    fun markAllAsRead(user: User): Int {
        val userId = user.id ?: return 0
        val unreadNotifications = notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
        unreadNotifications.forEach { it.markRead() }
        return unreadNotifications.size
    }

    @Transactional
    fun publishLevelUpNotification(user: User, newLevel: UserLevel, postCount: Long) {
        val referenceDate = LocalDate.now()
        createIfAbsent(
            user = user,
            title = "레벨 업 축하!",
            message = "Lv.${newLevel.level} ${newLevel.displayName} 달성! 누적 인증 ${postCount}회예요.",
            severity = NotificationSeverity.SUCCESS,
            code = "LEVEL_UP_${newLevel.level}",
            referenceDate = referenceDate,
        )
    }

    private fun createIfAbsent(
        user: User,
        title: String,
        message: String,
        severity: NotificationSeverity,
        code: String,
        referenceDate: LocalDate,
    ) {
        val userId = user.id ?: return
        if (notificationRepository.existsByUserIdAndCodeAndReferenceDate(userId, code, referenceDate)) return

        notificationRepository.save(
            UserNotification(
                user = user,
                title = title,
                message = message,
                severity = severity,
                code = code,
                referenceDate = referenceDate,
                isRead = false,
            )
        )
    }

    private fun calculateStreak(postDateSet: Set<LocalDate>, today: LocalDate): Int {
        if (postDateSet.isEmpty()) return 0

        var streak = 0
        var cursor = if (postDateSet.contains(today)) today else today.minusDays(1)

        if (!postDateSet.contains(cursor)) return 0

        while (postDateSet.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }

        val latestDate = postDateSet.maxOrNull() ?: return streak
        val gapDays = ChronoUnit.DAYS.between(latestDate, today)
        if (gapDays >= 2) return 0

        return streak
    }

    private fun UserNotification.toDto(): UserNotificationResponseDto = UserNotificationResponseDto(
        id = id,
        title = title,
        message = message,
        severity = severity,
        code = code,
        referenceDate = referenceDate,
        isRead = isRead,
        createdAt = createdAt,
    )
}
