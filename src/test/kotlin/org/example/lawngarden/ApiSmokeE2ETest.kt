package org.example.lawngarden

import org.assertj.core.api.Assertions.assertThat
import org.example.lawngarden.domain.geeknews.entity.GeekNewsArticle
import org.example.lawngarden.domain.geeknews.repository.GeekNewsArticleRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = [
        "DATABASE_URL=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "GITHUB_CLIENT_ID=test-client",
        "GITHUB_CLIENT_SECRET=test-secret",
        "SMTP_USERNAME=test@example.com",
        "SMTP_PASSWORD=test-password",
        "FRONT_CALLBACK_URL=http://localhost:5173/oauth/github",
        "image.path=build/test-images",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    ]
)
class ApiSmokeE2ETest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val geekNewsArticleRepository: GeekNewsArticleRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `new api smoke test`() {
        val userToken = registerAndLogin("user")
        val adminName = "admin_${UUID.randomUUID().toString().take(8)}"
        registerUser(adminName, "admin_${UUID.randomUUID().toString().take(8)}@example.com", "pw1234")
        jdbcTemplate.update("UPDATE users SET role = 'ADMIN' WHERE username = ?", adminName)
        val adminToken = login(adminName, "pw1234")

        val meResponse = authedGet(userToken, "/api/v1/users/me")
        assertThat(meResponse.statusCode).isEqualTo(HttpStatus.OK)

        val levelProgressResponse = authedGet(userToken, "/api/v1/users/me/level-progress")
        assertThat(levelProgressResponse.statusCode).isEqualTo(HttpStatus.OK)

        val levelHistoryResponse = authedGet(userToken, "/api/v1/users/me/level-history?size=5")
        assertThat(levelHistoryResponse.statusCode).isEqualTo(HttpStatus.OK)

        val mailSettingsUpdateBody = mapOf(
            "status" to "ON",
            "preferredDays" to listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
            "preferredHour" to 8,
            "categories" to listOf("BACKEND", "AI"),
        )
        val mailSettingsUpdateResponse = authedExchange(
            token = userToken,
            path = "/api/v1/mails/me/settings",
            method = HttpMethod.PUT,
            body = mailSettingsUpdateBody,
        )
        assertThat(mailSettingsUpdateResponse.statusCode).isEqualTo(HttpStatus.OK)

        val mailSettingsGetResponse = authedGet(userToken, "/api/v1/mails/me/settings")
        assertThat(mailSettingsGetResponse.statusCode).isEqualTo(HttpStatus.OK)

        val refreshedNotificationResponse = authedExchange(
            token = userToken,
            path = "/api/v1/notifications/me/refresh",
            method = HttpMethod.POST,
            body = null,
        )
        assertThat(refreshedNotificationResponse.statusCode).isEqualTo(HttpStatus.OK)

        val notificationListResponse = authedGet(userToken, "/api/v1/notifications/me")
        assertThat(notificationListResponse.statusCode).isEqualTo(HttpStatus.OK)

        val article = geekNewsArticleRepository.save(
            GeekNewsArticle(
                sourceId = "test-source-${UUID.randomUUID()}",
                title = "Test title",
                link = "https://example.com/${UUID.randomUUID()}",
                summary = "Test summary",
                publishedAt = LocalDateTime.now(),
            )
        )

        val listResponse = authedGet(userToken, "/api/v1/geeknews?page=0&size=10")
        assertThat(listResponse.statusCode).isEqualTo(HttpStatus.OK)

        val bookmarkResponse = authedExchange(
            token = userToken,
            path = "/api/v1/geeknews/${article.id}/bookmark?bookmarked=true",
            method = HttpMethod.POST,
            body = null,
        )
        assertThat(bookmarkResponse.statusCode).isEqualTo(HttpStatus.OK)

        val readResponse = authedExchange(
            token = userToken,
            path = "/api/v1/geeknews/${article.id}/read",
            method = HttpMethod.POST,
            body = null,
        )
        assertThat(readResponse.statusCode).isEqualTo(HttpStatus.OK)

        val bookmarksResponse = authedGet(userToken, "/api/v1/geeknews/bookmarks/me?page=0&size=10")
        assertThat(bookmarksResponse.statusCode).isEqualTo(HttpStatus.OK)

        val adminSyncResponse = authedExchange(
            token = adminToken,
            path = "/api/v1/admin/users/levels/sync",
            method = HttpMethod.POST,
            body = null,
        )
        assertThat(adminSyncResponse.statusCode).isEqualTo(HttpStatus.OK)

        val markAllNotificationReadResponse = authedExchange(
            token = userToken,
            path = "/api/v1/notifications/me/read-all",
            method = HttpMethod.POST,
            body = null,
        )
        assertThat(markAllNotificationReadResponse.statusCode).isEqualTo(HttpStatus.OK)

        val adminLogsResponse = authedGet(adminToken, "/api/v1/admin/geeknews/sync-logs?page=0&size=10")
        assertThat(adminLogsResponse.statusCode).isEqualTo(HttpStatus.OK)

        val systemStatus = restTemplate.getForEntity(baseUrl("/api/v1/system/status"), String::class.java)
        assertThat(systemStatus.statusCode).isEqualTo(HttpStatus.OK)

        val errorCodes = restTemplate.getForEntity(baseUrl("/api/v1/system/error-codes"), String::class.java)
        assertThat(errorCodes.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun registerAndLogin(prefix: String): String {
        val username = "${prefix}_${UUID.randomUUID().toString().take(8)}"
        registerUser(username, "${prefix}_${UUID.randomUUID().toString().take(8)}@example.com", "pw1234")
        return login(username, "pw1234")
    }

    private fun registerUser(username: String, email: String, password: String) {
        val body = mapOf(
            "username" to username,
            "email" to email,
            "password" to password,
            "type" to "NONE",
        )
        val response = restTemplate.postForEntity(baseUrl("/api/v1/users/register"), body, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun login(username: String, password: String): String {
        val body = mapOf("username" to username, "password" to password)
        val response = restTemplate.postForEntity(baseUrl("/api/v1/auth/login"), body, Map::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val accessToken = response.body?.get("accessToken")?.toString().orEmpty()
        assertThat(accessToken).startsWith("Bearer ")
        return accessToken
    }

    private fun authedGet(token: String, path: String) =
        authedExchange(token = token, path = path, method = HttpMethod.GET, body = null)

    private fun authedExchange(token: String, path: String, method: HttpMethod, body: Any?) = run {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(HttpHeaders.AUTHORIZATION, token)
        val request = HttpEntity(body, headers)
        restTemplate.exchange(baseUrl(path), method, request, String::class.java)
    }

    private fun baseUrl(path: String): String = "http://localhost:$port$path"
}
