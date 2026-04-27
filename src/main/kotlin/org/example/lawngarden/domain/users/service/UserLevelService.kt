package org.example.lawngarden.domain.users.service

import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.enums.UserLevel
import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserLevelService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun syncAllUserLevels(): Int {
        var updatedRows = 0

        userRepository.findAll().forEach { user ->
            val userId = user.id ?: return@forEach
            val postCount = postRepository.countByUserId(userId)
            val calculatedLevel = UserLevel.fromPostCount(postCount)

            if (user.level != calculatedLevel.level) {
                user.updateLevel(calculatedLevel.level)
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
            user.updateLevel(calculatedLevel.level)
        }
    }
}
