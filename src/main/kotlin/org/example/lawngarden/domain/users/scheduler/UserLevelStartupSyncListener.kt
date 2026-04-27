package org.example.lawngarden.domain.users.scheduler

import org.example.lawngarden.domain.users.service.UserLevelService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserLevelStartupSyncListener(
    private val userLevelService: UserLevelService,
) {
    private val logger = LoggerFactory.getLogger(UserLevelStartupSyncListener::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun syncOnStartup() {
        runCatching { userLevelService.syncAllUserLevels() }
            .onSuccess { updatedRows ->
                logger.info("User level sync completed on startup. updatedRows={}", updatedRows)
            }
            .onFailure { ex ->
                logger.error("User level sync failed on startup", ex)
            }
    }
}
