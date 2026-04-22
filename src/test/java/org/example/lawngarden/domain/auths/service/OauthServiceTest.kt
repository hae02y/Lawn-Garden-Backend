package org.example.lawngarden.domain.auths.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OauthServiceTest(
    @Autowired private val oauthService: OauthService,
) {
    @Test
    fun generateRedirectUrl() {
        //given

        //when
        val generateRedirectUrl = oauthService.generateRedirectUrl()

        //then
        println(generateRedirectUrl)
        Assertions.assertThat(generateRedirectUrl).isNotBlank

    }

}