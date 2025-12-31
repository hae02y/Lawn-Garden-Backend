package org.example.lawngarden;

import org.assertj.core.api.Assertions.*
import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.push.entity.MailContents
import org.example.lawngarden.domain.push.enums.MailCategory
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.push.repository.MailContentsRepository
import org.example.lawngarden.domain.push.repository.MailRepository
import org.example.lawngarden.domain.push.service.MailService;
import org.example.lawngarden.domain.users.entity.User;
import org.example.lawngarden.domain.users.repository.UserRepository
import org.example.lawngarden.domain.users.service.UserService
import org.hibernate.usertype.UserType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class LawngardenApplicationTests(
    @Autowired private val mailService: MailService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val mailRepository: MailRepository,
    @Autowired private val mailContentsRepository: MailContentsRepository,
    @Autowired private val mailSender: JavaMailSender
) {
    @BeforeEach
    fun setUp() {
        val user = User(
            null, "haeyoung", "godud1118@naver.com", "qwe123,.", role = Role.USER, type = null,
            post = null,
            like = null,
            level = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deletedAt = null
        )
        userRepository.save(user)
    }

    @Test
    fun contextLoads() {

        val findById = userRepository.findById(1).get()

        val createMail = mailService.createMail(findById)

        println("hi $createMail")

        assertThat(createMail).isNotNull;
        assertThat(createMail.status).isEqualTo(MailStatus.ON);
    }


    @Test
    @DisplayName("Insert 테스트")
    fun insert() {
        val mailContents =
            MailContents(null, "name", "contents", MailCategory.NONE, "test.com", Instant.now(), Instant.now())
        mailContentsRepository.save(mailContents)
    }


}
