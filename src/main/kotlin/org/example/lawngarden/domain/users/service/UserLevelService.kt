package org.example.lawngarden.domain.users.service

import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.dto.UserLevelHistoryResponseDto
import org.example.lawngarden.domain.users.dto.UserLevelProgressResponseDto
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.entity.UserLevelHistory
import org.example.lawngarden.domain.users.enums.UserLevel
import org.example.lawngarden.domain.users.repository.UserLevelHistoryRepository
import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserLevelService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val userLevelHistoryRepository: UserLevelHistoryRepository,
) {
    @Transactional
    fun syncAllUserLevels(): Int {
        var updatedRows = 0

        userRepository.findAll().forEach { user ->
            val userId = user.id ?: return@forEach
            val postCount = postRepository.countByUserId(userId)
            val calculatedLevel = UserLevel.fromPostCount(postCount)

            if (user.level != calculatedLevel.level) {
                updateLevelWithHistory(user = user, newLevel = calculatedLevel, postCount = postCount)
                updatedRows++
            }
        }

        return updatedRows
    }

    @Transactional
    fun syncUserLevel(userId: Long?) {
        if (userId == null) return

        val user = userRepository.findByIdOrNull(userId) ?: return
        val postCount = postRepository.countByUserId(userId)
        val calculatedLevel = UserLevel.fromPostCount(postCount)

        if (user.level != calculatedLevel.level) {
            updateLevelWithHistory(user = user, newLevel = calculatedLevel, postCount = postCount)
        }
    }

    @Transactional(readOnly = true)
    fun getLevelProgress(userId: Long): UserLevelProgressResponseDto {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw java.util.NoSuchElementException("User with ID $userId not found")
        val postCount = postRepository.countByUserId(userId)
        val currentLevel = UserLevel.fromLevel(user.level)
        val nextLevel = UserLevel.nextLevel(currentLevel)
        val remainingCount = if (nextLevel == null) 0L else (nextLevel.minPostCount - postCount).coerceAtLeast(0)

        return UserLevelProgressResponseDto(
            currentLevel = currentLevel.level,
            currentLevelName = currentLevel.displayName,
            currentBadge = currentLevel.badgeLabel,
            postCount = postCount,
            nextLevel = nextLevel?.level,
            nextLevelName = nextLevel?.displayName,
            nextLevelMinPostCount = nextLevel?.minPostCount,
            remainingPostCount = remainingCount,
        )
    }

    @Transactional(readOnly = true)
    fun getLevelHistories(userId: Long, size: Int): List<UserLevelHistoryResponseDto> {
        if (size <= 0) return emptyList()

        val pageable = PageRequest.of(0, size.coerceAtMost(50))
        return userLevelHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map {
                val previous = UserLevel.fromLevel(it.previousLevel)
                val current = UserLevel.fromLevel(it.newLevel)
                UserLevelHistoryResponseDto(
                    id = it.id,
                    previousLevel = it.previousLevel,
                    previousLevelName = previous.displayName,
                    newLevel = it.newLevel,
                    newLevelName = current.displayName,
                    postCount = it.postCount,
                    changedAt = it.createdAt,
                )
            }
    }

    private fun updateLevelWithHistory(user: User, newLevel: UserLevel, postCount: Long) {
        val previousLevel = user.level
        user.updateLevel(newLevel.level)
        userLevelHistoryRepository.save(
            UserLevelHistory(
                user = user,
                previousLevel = previousLevel,
                newLevel = newLevel.level,
                postCount = postCount,
            )
        )
    }
}
