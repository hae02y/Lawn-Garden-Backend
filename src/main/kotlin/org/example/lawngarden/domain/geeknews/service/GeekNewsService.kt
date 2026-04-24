package org.example.lawngarden.domain.geeknews.service

import org.example.lawngarden.domain.geeknews.dto.GeekNewsResponseDto
import org.example.lawngarden.domain.geeknews.entity.GeekNewsArticle
import org.example.lawngarden.domain.geeknews.repository.GeekNewsArticleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
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
) {
    private val rssUrl = "https://feeds.feedburner.com/geeknews-feed"
    private val seoulZone: ZoneId = ZoneId.of("Asia/Seoul")

    fun getGeekNews(pageable: Pageable, keyword: String?): Page<GeekNewsResponseDto> {
        if (geekNewsArticleRepository.count() == 0L) {
            runCatching { syncGeekNews(50) }
        }

        val page = if (keyword.isNullOrBlank()) {
            geekNewsArticleRepository.findAllByOrderByPublishedAtDescIdDesc(pageable)
        } else {
            geekNewsArticleRepository.findAllByTitleContainingIgnoreCaseOrderByPublishedAtDescIdDesc(keyword, pageable)
        }

        return page.map {
            GeekNewsResponseDto(
                id = it.id,
                sourceId = it.sourceId,
                title = it.title,
                link = it.link,
                summary = it.summary,
                publishedAt = it.publishedAt,
            )
        }
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
        val sourceId = item.guid?.takeIf { it.isNotBlank() } ?: item.link
        if (sourceId.isBlank() || item.title.isBlank() || item.link.isBlank()) return false
        if (geekNewsArticleRepository.existsBySourceId(sourceId)) return false

        val publishedAt = publishedInstant?.atZone(seoulZone)?.toLocalDateTime()
        geekNewsArticleRepository.save(
            GeekNewsArticle(
                sourceId = sourceId,
                title = item.title,
                link = item.link,
                summary = item.description,
                publishedAt = publishedAt,
            )
        )
        return true
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
