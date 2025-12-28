package org.example.lawngarden.domain.push.service

import jakarta.transaction.Transactional
import org.example.lawngarden.domain.push.entity.Mail
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.repository.MailRepository
import org.example.lawngarden.domain.users.entity.User
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailRepository: MailRepository,
    private val mailSender: JavaMailSender
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