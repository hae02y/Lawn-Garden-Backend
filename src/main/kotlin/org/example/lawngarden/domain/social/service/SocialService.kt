package org.example.lawngarden.domain.social.service

import org.example.lawngarden.domain.social.dto.CheerStatusResponseDto
import org.example.lawngarden.domain.social.dto.WeeklyLeaderboardItemDto
import org.example.lawngarden.domain.social.entity.GardenCheer
import org.example.lawngarden.domain.social.enums.CheerType
import org.example.lawngarden.domain.social.repository.GardenCheerRepository
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.repository.UserRepository
import org.example.lawngarden.domain.posts.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SocialService(
    private val gardenCheerRepository: GardenCheerRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun cheerUser(fromUser: User, targetUserId: Long, type: CheerType): CheerStatusResponseDto {
        val fromUserId = fromUser.id ?: throw NoSuchElementException("User id not found")
        if (fromUserId == targetUserId) {
            throw IllegalArgumentException("자기 자신에게는 응원을 보낼 수 없습니다.")
        }

        val targetUser = userRepository.findById(targetUserId)
            .orElseThrow { NoSuchElementException("Target user not found. id=$targetUserId") }
        val today = LocalDate.now()

        val alreadyCheered = gardenCheerRepository.existsByFromUserIdAndToUserIdAndCheerDate(fromUserId, targetUserId, today)
        if (alreadyCheered) {
            return getCheerStatus(fromUser, targetUserId)
        }

        gardenCheerRepository.save(
            GardenCheer(
                fromUser = fromUser,
                toUser = targetUser,
                type = type,
                cheerDate = today,
            )
        )

        return getCheerStatus(fromUser, targetUserId)
    }

    @Transactional(readOnly = true)
    fun getCheerStatus(fromUser: User, targetUserId: Long): CheerStatusResponseDto {
        val fromUserId = fromUser.id ?: throw NoSuchElementException("User id not found")
        val today = LocalDate.now()
        val canCheerToday = !gardenCheerRepository.existsByFromUserIdAndToUserIdAndCheerDate(fromUserId, targetUserId, today)
        val receivedToday = gardenCheerRepository.countByToUserIdAndCheerDate(targetUserId, today)
        val receivedTotal = gardenCheerRepository.countByToUserId(targetUserId)

        return CheerStatusResponseDto(
            canCheerToday = canCheerToday,
            receivedTodayCount = receivedToday,
            receivedTotalCount = receivedTotal,
        )
    }

    @Transactional(readOnly = true)
    fun getWeeklyLeaderboard(limit: Int): List<WeeklyLeaderboardItemDto> {
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val weekEnd = weekStart.plusDays(6)
        val prevWeekStart = weekStart.minusDays(7)
        val prevWeekEnd = weekEnd.minusDays(7)

        return userRepository.findAll()
            .mapNotNull { user ->
                val userId = user.id ?: return@mapNotNull null
                val weeklyCount = postRepository.countByUserIdAndCreatedDateBetween(userId, weekStart, weekEnd)
                val prevWeeklyCount = postRepository.countByUserIdAndCreatedDateBetween(userId, prevWeekStart, prevWeekEnd)
                val postDates = postRepository.findDistinctCreatedDatesByUserId(userId).toSet()
                val streakDays = calculateStreak(postDates, today)
                val growthRate = if (prevWeeklyCount == 0L) {
                    if (weeklyCount == 0L) 0.0 else 100.0
                } else {
                    ((weeklyCount - prevWeeklyCount).toDouble() / prevWeeklyCount.toDouble()) * 100.0
                }

                WeeklyLeaderboardItemDto(
                    userId = userId,
                    username = user.username,
                    weeklyPostCount = weeklyCount,
                    streakDays = streakDays,
                    growthRate = String.format("%.1f", growthRate).toDouble(),
                )
            }
            .sortedWith(
                compareByDescending<WeeklyLeaderboardItemDto> { it.weeklyPostCount }
                    .thenByDescending { it.streakDays }
                    .thenByDescending { it.growthRate }
            )
            .take(limit.coerceIn(1, 50))
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
