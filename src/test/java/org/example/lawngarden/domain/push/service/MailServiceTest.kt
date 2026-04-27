package org.example.lawngarden.domain.push.service

import org.example.lawngarden.domain.auths.enums.LoginType
import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.push.entity.Mail
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.repository.MailRepository
import org.example.lawngarden.domain.users.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.javamail.JavaMailSender

@ExtendWith(MockitoExtension::class)
class MailServiceTest {
    @Mock
    private lateinit var mailRepository: MailRepository

    @Mock
    private lateinit var mailSender: JavaMailSender

    private lateinit var mailService: MailService

    @BeforeEach
    fun setUp() {
        mailService = MailService(mailRepository, mailSender)
    }

    @Test
    fun changeMailStatus_createsMailWhenMissing() {
        val user = testUser()

        `when`(mailRepository.findByUser(user)).thenReturn(null)
        `when`(mailRepository.save(any(Mail::class.java))).thenAnswer { invocation -> invocation.arguments[0] as Mail }

        val result = mailService.changeMailStatus(MailStatus.OFF, user)

        assertEquals(MailStatus.OFF, result)
        verify(mailRepository).save(any(Mail::class.java))
    }

    @Test
    fun getMailStatus_returnsExistingStatus() {
        val user = testUser()
        `when`(mailRepository.findByUser(user)).thenReturn(Mail(user = user, status = MailStatus.OFF))

        val result = mailService.getMailStatus(user)

        assertEquals(MailStatus.OFF, result)
    }

    private fun testUser(): User = User(
        username = "tester",
        email = "tester@example.com",
        password = "encoded",
        post = mutableListOf(),
        role = Role.USER,
        type = LoginType.NONE,
    )

}
