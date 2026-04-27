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
    private val mailSender: JavaMailSender,
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
    fun changeMailStatus(status: MailStatus, user: User): MailStatus {
        val findByUser = mailRepository.findByUser(user) ?: mailRepository.save(
            Mail(
                user = user,
                status = status,
            )
        )
        findByUser.changeStatus(status)
        return findByUser.status
    }

    @Transactional
    fun getMailStatus(user: User): MailStatus {
        val findByUser = mailRepository.findByUser(user) ?: createMail(user)
        return findByUser.status
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
