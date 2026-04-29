package org.example.lawngarden.domain.geeknews.service

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.example.lawngarden.domain.geeknews.dto.GeekNewsListResponseDto
import org.example.lawngarden.domain.geeknews.dto.GeekNewsResponseDto
import org.example.lawngarden.domain.geeknews.dto.GeekNewsStateResponseDto
import org.example.lawngarden.domain.geeknews.entity.GeekNewsArticle
import org.example.lawngarden.domain.geeknews.entity.GeekNewsUserState
import org.example.lawngarden.domain.geeknews.repository.GeekNewsArticleRepository
import org.example.lawngarden.domain.geeknews.repository.GeekNewsUserStateRepository
import org.example.lawngarden.domain.users.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.w3c.dom.Element
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

@Service
class GeekNewsService(
    private val geekNewsArticleRepository: GeekNewsArticleRepository,
    private val geekNewsUserStateRepository: GeekNewsUserStateRepository,
) {
    private val rssUrl = "https://feeds.feedburner.com/geeknews-feed"
    private val seoulZone: ZoneId = ZoneId.of("Asia/Seoul")

    @Transactional(readOnly = true)
    fun getGeekNews(pageable: Pageable, keyword: String?, user: User?): Page<GeekNewsResponseDto> {
        if (geekNewsArticleRepository.count() == 0L) {
            runCatching { syncGeekNews(50) }
        }

        val page = if (keyword.isNullOrBlank()) {
            geekNewsArticleRepository.findAllByOrderByPublishedAtDescIdDesc(pageable)
        } else {
            geekNewsArticleRepository.findAllByTitleContainingIgnoreCaseOrderByPublishedAtDescIdDesc(keyword, pageable)
        }

        val articleIds = page.content.mapNotNull { it.id }
        val stateMap = if (user?.id == null || articleIds.isEmpty()) {
            emptyMap()
        } else {
            geekNewsUserStateRepository
                .findAllByUserIdAndArticleIdIn(user.id, articleIds)
                .associateBy { it.article.id }
        }

        val content = page.content.map { article ->
            val state = article.id?.let { stateMap[it] }
            GeekNewsResponseDto(
                id = article.id,
                sourceId = article.sourceId,
                title = sanitizeText(article.title) ?: article.title,
                link = article.link,
                summary = sanitizeText(article.summary),
                publishedAt = article.publishedAt,
                bookmarked = state?.bookmarked ?: false,
                read = state?.readAt != null,
                readAt = state?.readAt,
            )
        }

        return PageImpl(content, pageable, page.totalElements)
    }

    @Transactional(readOnly = true)
    fun getMyBookmarkedGeekNews(pageable: Pageable, user: User): GeekNewsListResponseDto {
        val userId = user.id ?: throw NoSuchElementException("User id not found")
        val page = geekNewsUserStateRepository.findAllByUserIdAndBookmarkedTrueOrderByModifiedAtDesc(userId, pageable)

        val content = page.content.map { state ->
            val article = state.article
            GeekNewsResponseDto(
                id = article.id,
                sourceId = article.sourceId,
                title = sanitizeText(article.title) ?: article.title,
                link = article.link,
                summary = sanitizeText(article.summary),
                publishedAt = article.publishedAt,
                bookmarked = state.bookmarked,
                read = state.readAt != null,
                readAt = state.readAt,
            )
        }

        return GeekNewsListResponseDto(
            items = content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            hasNext = page.hasNext(),
        )
    }

    @Transactional
    fun toggleBookmark(articleId: Long, bookmarked: Boolean, user: User): GeekNewsStateResponseDto {
        val userId = user.id ?: throw NoSuchElementException("User id not found")
        val article = geekNewsArticleRepository.findById(articleId)
            .orElseThrow { NoSuchElementException("GeekNews article not found. id=$articleId") }
        val state = geekNewsUserStateRepository.findByUserIdAndArticleId(userId, articleId)
            ?: geekNewsUserStateRepository.save(GeekNewsUserState(user = user, article = article))

        state.bookmarked = bookmarked
        return GeekNewsStateResponseDto(
            articleId = articleId,
            bookmarked = state.bookmarked,
            read = state.readAt != null,
            readAt = state.readAt,
        )
    }

    @Transactional
    fun markRead(articleId: Long, user: User): GeekNewsStateResponseDto {
        val userId = user.id ?: throw NoSuchElementException("User id not found")
        val article = geekNewsArticleRepository.findById(articleId)
            .orElseThrow { NoSuchElementException("GeekNews article not found. id=$articleId") }
        val state = geekNewsUserStateRepository.findByUserIdAndArticleId(userId, articleId)
            ?: geekNewsUserStateRepository.save(GeekNewsUserState(user = user, article = article))

        state.markRead(LocalDateTime.now())
        return GeekNewsStateResponseDto(
            articleId = articleId,
            bookmarked = state.bookmarked,
            read = true,
            readAt = state.readAt,
        )
    }

    fun syncGeekNews(maxItems: Int = 50): Int {
        val items = fetchRssItems().take(maxItems)
        var inserted = 0

        for (item in items) {
            if (saveIfNew(item, parsePublishedInstant(item.pubDate))) inserted++
        }

        return inserted
    }

    fun syncGeekNewsDaily(maxItems: Int = 300): Int {
        val cutoffInstant = ZonedDateTime.now(seoulZone).minusDays(1).toInstant()
        val items = fetchRssItems()
            .map { item -> item to parsePublishedInstant(item.pubDate) }
            .filter { (_, publishedInstant) -> publishedInstant != null && publishedInstant >= cutoffInstant }
            .take(maxItems)

        var inserted = 0
        for ((item, publishedInstant) in items) {
            if (saveIfNew(item, publishedInstant)) inserted++
        }

        return inserted
    }

    private fun fetchRssItems(): List<RssItem> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        URL(rssUrl).openStream().use { input ->
            val document = builder.parse(input)
            val rssItems = document.getElementsByTagName("item")
            if (rssItems.length > 0) {
                return (0 until rssItems.length).mapNotNull { index ->
                    val element = rssItems.item(index) as? Element ?: return@mapNotNull null
                    RssItem(
                        title = elementText(element, "title") ?: "",
                        link = elementText(element, "link") ?: "",
                        description = elementText(element, "description"),
                        guid = elementText(element, "guid"),
                        pubDate = elementText(element, "pubDate"),
                    )
                }
            }

            val atomEntries = document.getElementsByTagName("entry")
            return (0 until atomEntries.length).mapNotNull { index ->
                val element = atomEntries.item(index) as? Element ?: return@mapNotNull null
                RssItem(
                    title = elementText(element, "title") ?: "",
                    link = atomLink(element) ?: "",
                    description = elementText(element, "summary") ?: elementText(element, "content"),
                    guid = elementText(element, "id"),
                    pubDate = elementText(element, "published") ?: elementText(element, "updated"),
                )
            }
        }
    }

    private fun parsePublishedInstant(pubDate: String?): Instant? {
        if (pubDate.isNullOrBlank()) return null
        return runCatching {
            ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
        }.recoverCatching {
            OffsetDateTime.parse(pubDate).toInstant()
        }.recoverCatching {
            Instant.parse(pubDate)
        }.getOrNull()
    }

    private fun saveIfNew(item: RssItem, publishedInstant: Instant?): Boolean {
        val cleanLink = item.link.trim()
        val cleanTitle = sanitizeText(item.title) ?: return false
        val cleanSummary = sanitizeText(item.description)
        val sourceId = item.guid?.trim()?.takeIf { it.isNotBlank() } ?: cleanLink

        if (sourceId.isBlank() || cleanLink.isBlank()) return false
        if (geekNewsArticleRepository.existsBySourceId(sourceId)) return false

        val publishedAt = publishedInstant?.atZone(seoulZone)?.toLocalDateTime()
        geekNewsArticleRepository.save(
            GeekNewsArticle(
                sourceId = sourceId,
                title = cleanTitle,
                link = cleanLink,
                summary = cleanSummary,
                publishedAt = publishedAt,
            )
        )
        return true
    }

    private fun sanitizeText(raw: String?): String? {
        if (raw.isNullOrBlank()) return null

        val unescaped = Parser.unescapeEntities(raw, false)
        val plainText = Jsoup.parse(unescaped).text()
        val normalized = plainText.replace(Regex("\\s+"), " ").trim()

        return normalized.takeIf { it.isNotBlank() }
    }

    private fun elementText(element: Element, tagName: String): String? {
        val list = element.getElementsByTagName(tagName)
        if (list.length == 0) return null
        return list.item(0)?.textContent?.trim()
    }

    private fun atomLink(element: Element): String? {
        val links = element.getElementsByTagName("link")
        if (links.length == 0) return null

        val preferred = (0 until links.length)
            .mapNotNull { idx -> links.item(idx) as? Element }
            .firstOrNull { (it.getAttribute("rel") ?: "").ifBlank { "alternate" } == "alternate" }

        val target = preferred ?: (links.item(0) as? Element)
        val href = target?.getAttribute("href")?.trim()

        return if (href.isNullOrBlank()) {
            target?.textContent?.trim()?.takeIf { it.isNotBlank() }
        } else {
            href
        }
    }

    private data class RssItem(
        val title: String,
        val link: String,
        val description: String?,
        val guid: String?,
        val pubDate: String?,
    )
}
