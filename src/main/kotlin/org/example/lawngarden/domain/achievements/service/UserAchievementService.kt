package org.example.lawngarden.domain.achievements.service

import org.example.lawngarden.domain.achievements.dto.UserAchievementResponseDto
import org.example.lawngarden.domain.achievements.entity.UserAchievement
import org.example.lawngarden.domain.achievements.enums.AchievementCode
import org.example.lawngarden.domain.achievements.repository.UserAchievementRepository
import org.example.lawngarden.domain.notifications.enums.NotificationSeverity
import org.example.lawngarden.domain.notifications.service.UserNotificationService
import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.enums.UserLevel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UserAchievementService(
    private val userAchievementRepository: UserAchievementRepository,
    private val postRepository: PostRepository,
    private val notificationService: UserNotificationService,
) {
    @Transactional
    fun refreshMyAchievements(user: User): List<UserAchievementResponseDto> {
        val userId = user.id ?: return emptyList()

        val postDates = postRepository.findDistinctCreatedDatesByUserId(userId)
        val postDateSet = postDates.toSet()
        val postCount = postRepository.countByUserId(userId)
        val streakDays = calculateStreak(postDateSet, LocalDate.now())
        val currentLevel = UserLevel.fromPostCount(postCount)

        unlockIfSatisfied(user, AchievementCode.FIRST_WATERING, postCount >= 1)
        unlockIfSatisfied(user, AchievementCode.STREAK_7, streakDays >= 7)
        unlockIfSatisfied(user, AchievementCode.STREAK_30, streakDays >= 30)
        unlockIfSatisfied(user, AchievementCode.LEVEL_3, currentLevel.level >= 3)
        unlockIfSatisfied(user, AchievementCode.LEVEL_5, currentLevel.level >= 5)

        val monthStart = LocalDate.now().withDayOfMonth(1)
        val monthCount = postDates.count { it != null && !it.isBefore(monthStart) }
        unlockIfSatisfied(user, AchievementCode.MONTHLY_12, monthCount >= 12)

        return getMyAchievements(user)
    }

    @Transactional(readOnly = true)
    fun getMyAchievements(user: User): List<UserAchievementResponseDto> {
        val userId = user.id ?: return emptyList()
        val unlockedMap = userAchievementRepository.findAllByUserIdOrderByUnlockedAtDesc(userId)
            .associateBy { it.code }

        return AchievementCode.entries.map { code ->
            val unlocked = unlockedMap[code]
            UserAchievementResponseDto(
                code = code,
                title = code.title,
                description = code.description,
                unlocked = unlocked != null,
                unlockedAt = unlocked?.unlockedAt,
            )
        }
    }

    private fun unlockIfSatisfied(user: User, code: AchievementCode, satisfied: Boolean) {
        val userId = user.id ?: return
        if (!satisfied) return
        if (userAchievementRepository.existsByUserIdAndCode(userId, code)) return

        userAchievementRepository.save(UserAchievement(user = user, code = code))
        notificationService.publishCustomNotification(
            user = user,
            title = "새 업적 달성!",
            message = "${code.title} 업적을 획득했어요.",
            severity = NotificationSeverity.SUCCESS,
            code = "ACHIEVEMENT_${code.name}",
            deepLink = "/mygarden/${userId}",
            referenceDate = LocalDate.now(),
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
        return streak
    }
}
