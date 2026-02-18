package org.example.lawngarden.common.config

import org.example.lawngarden.domain.push.service.MailService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
class MailTestRunner(
    private val mailService: MailService,
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(MailTestRunner::class.java)

    override fun run(vararg args: String?) {
        logger.info("Mail test runner started")
        try {
            mailService.sendDailyInterviewEmails()
            logger.info("Mail test runner finished")
        } catch (ex: Exception) {
            logger.error("Mail test runner failed", ex)
        }
    }
}
