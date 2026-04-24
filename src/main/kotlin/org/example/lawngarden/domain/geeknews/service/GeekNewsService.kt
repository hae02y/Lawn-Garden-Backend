package org.example.lawngarden.domain.geeknews.service

import org.example.lawngarden.domain.geeknews.dto.GeekNewsResponseDto
import org.example.lawngarden.domain.geeknews.entity.GeekNewsArticle
import org.example.lawngarden.domain.geeknews.repository.GeekNewsArticleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

@Service
class GeekNewsService(
    private val geekNewsArticleRepository: GeekNewsArticleRepository,
) {
    private val rssUrl = "https://news.hada.io/rss/news"

    fun getGeekNews(pageable: Pageable, keyword: String?): Page<GeekNewsResponseDto> {
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
            val sourceId = item.guid?.takeIf { it.isNotBlank() } ?: item.link
            if (sourceId.isBlank() || item.title.isBlank() || item.link.isBlank()) continue
            if (geekNewsArticleRepository.existsBySourceId(sourceId)) continue

            geekNewsArticleRepository.save(
                GeekNewsArticle(
                    sourceId = sourceId,
                    title = item.title,
                    link = item.link,
                    summary = item.description,
                    publishedAt = parsePublishedAt(item.pubDate),
                )
            )
            inserted++
        }

        return inserted
    }

    private fun fetchRssItems(): List<RssItem> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        URL(rssUrl).openStream().use { input ->
            val document = builder.parse(input)
            val nodeList = document.getElementsByTagName("item")

            return (0 until nodeList.length).mapNotNull { index ->
                val element = nodeList.item(index) as? Element ?: return@mapNotNull null
                RssItem(
                    title = elementText(element, "title") ?: "",
                    link = elementText(element, "link") ?: "",
                    description = elementText(element, "description"),
                    guid = elementText(element, "guid"),
                    pubDate = elementText(element, "pubDate"),
                )
            }
        }
    }

    private fun parsePublishedAt(pubDate: String?): LocalDateTime? {
        if (pubDate.isNullOrBlank()) return null
        return runCatching {
            OffsetDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime()
        }.getOrNull()
    }

    private fun elementText(element: Element, tagName: String): String? {
        val list = element.getElementsByTagName(tagName)
        if (list.length == 0) return null
        return list.item(0)?.textContent?.trim()
    }

    private data class RssItem(
        val title: String,
        val link: String,
        val description: String?,
        val guid: String?,
        val pubDate: String?,
    )
}
