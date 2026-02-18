package org.example.lawngarden.domain.push.service

import jakarta.transaction.Transactional
import org.example.lawngarden.domain.push.entity.Mail
import org.example.lawngarden.domain.push.entity.MailContents
import org.example.lawngarden.domain.push.entity.MailProgress
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.repository.MailContentsRepository
import org.example.lawngarden.domain.push.repository.MailProgressRepository
import org.example.lawngarden.domain.push.repository.MailRepository
import org.example.lawngarden.domain.users.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.time.LocalDateTime

@Service
class MailService(
    private val mailRepository: MailRepository,
    private val mailSender: JavaMailSender,
    private val mailContentsRepository: MailContentsRepository,
    private val mailProgressRepository: MailProgressRepository,
    private val templateEngine: SpringTemplateEngine,
    @Value("\${spring.mail.username:}") private val mailFrom: String,
    @Value("\${app.mail.unsubscribe-base-url:}") private val unsubscribeBaseUrl: String,
) {
    fun sendSimpleMessage() {
        val message = SimpleMailMessage()
        message.from = "test@test.com"
        message.setTo("godud1118@gmail.com")
        message.subject = "subject"
        message.text = "text"
        mailSender.send(message)
    }

    @Transactional
    fun sendDailyInterviewEmails() {
        val subscribers = mailRepository.findAllByStatus(MailStatus.ON)
        if (subscribers.isEmpty()) return

        subscribers.forEach { mailSubscription ->
            val user = mailSubscription.user ?: return@forEach
            val progress = mailProgressRepository.findByUser(user) ?: mailProgressRepository.save(
                MailProgress(user = user)
            )

            if (progress.isCompleted) return@forEach

            val nextContent = resolveNextContent(progress)
            if (nextContent == null) {
                progress.isCompleted = true
                progress.lastSentAt = LocalDateTime.now()
                mailProgressRepository.save(progress)
                return@forEach
            }

            val context = Context().apply {
                setVariable("category", nextContent.category)
                setVariable("name", nextContent.name)
                setVariable("content", nextContent.content)
                setVariable("siteLink", nextContent.siteLink)
                setVariable("unsubscribeLink", buildUnsubscribeLink(user))
            }

            val htmlBody = templateEngine.process("mail/daily-interview", context)
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "UTF-8")

            helper.setTo(user.email)
            helper.setSubject("오늘의 면접 질문이 도착했어요")
            if (mailFrom.isNotBlank()) {
                helper.setFrom(mailFrom)
            }
            helper.setText(htmlBody, true)
            mailSender.send(mimeMessage)

            progress.lastSentMailContentId = nextContent.id
            progress.lastSentAt = LocalDateTime.now()
            mailProgressRepository.save(progress)
        }
    }

    private fun resolveNextContent(progress: MailProgress): MailContents? {
        val lastSentId = progress.lastSentMailContentId
        if (lastSentId.isNullOrBlank()) {
            return mailContentsRepository.findFirstByOrderByCreatedAtAsc()
        }

        val lastSent = mailContentsRepository.findById(lastSentId).orElse(null)
            ?: return mailContentsRepository.findFirstByOrderByCreatedAtAsc()

        val lastCreatedAt = lastSent.createdAt ?: return mailContentsRepository.findFirstByOrderByCreatedAtAsc()
        return mailContentsRepository.findFirstByCreatedAtAfterOrderByCreatedAtAsc(lastCreatedAt)
    }

    private fun buildUnsubscribeLink(user: User): String {
        if (unsubscribeBaseUrl.isBlank()) return "#"
        val delimiter = if (unsubscribeBaseUrl.contains("?")) "&" else "?"
        return "${unsubscribeBaseUrl}${delimiter}userId=${user.id}"
    }

    @Transactional
    fun changeMailStatus(status: MailStatus, user: User) {
        val findByUser = mailRepository.findByUser(user)
        findByUser?.changeStatus(status)
    }

    //메일등록
    fun createMail(user : User) : Mail {
        val findByUser = mailRepository.findByUser(user) ?: return mailRepository.save(
            Mail(
                user = user,
                status = MailStatus.ON,
            )
        )
        return findByUser
    }

}
