package org.example.lawngarden.domain.achievements.repository

import org.example.lawngarden.domain.achievements.entity.UserAchievement
import org.example.lawngarden.domain.achievements.enums.AchievementCode
import org.springframework.data.jpa.repository.JpaRepository

interface UserAchievementRepository : JpaRepository<UserAchievement, Long> {
    fun findAllByUserIdOrderByUnlockedAtDesc(userId: Long): List<UserAchievement>
    fun existsByUserIdAndCode(userId: Long, code: AchievementCode): Boolean
}
