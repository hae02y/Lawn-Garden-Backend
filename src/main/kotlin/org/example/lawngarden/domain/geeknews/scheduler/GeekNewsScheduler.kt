package org.example.lawngarden.domain.geeknews.scheduler

import org.example.lawngarden.domain.geeknews.service.GeekNewsService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class GeekNewsScheduler(
    private val geekNewsService: GeekNewsService,
) {
    private val logger = LoggerFactory.getLogger(GeekNewsScheduler::class.java)

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    fun syncDailyGeekNews() {
        val insertedCount = geekNewsService.syncGeekNewsDaily()
        logger.info("GeekNews daily sync completed. insertedCount={}", insertedCount)
    }
}
