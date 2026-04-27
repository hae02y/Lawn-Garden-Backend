package org.example.lawngarden.domain.users.service

import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.enums.UserLevel
import org.example.lawngarden.domain.users.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserLevelService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
) {
    private val logger = LoggerFactory.getLogger(UserLevelService::class.java)

    @Transactional
    fun syncAllUserLevels(): Int {
        return userRepository.syncLevelByPostCount()
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

    @EventListener(ApplicationReadyEvent::class)
    fun syncLevelOnStartup() {
        val updatedRows = syncAllUserLevels()
        logger.info("User level sync completed on startup. updatedRows={}", updatedRows)
    }
}
