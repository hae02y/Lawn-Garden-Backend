package org.example.lawngarden.domain.geeknews.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "geek_news_articles")
class GeekNewsArticle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val sourceId: String,

    @Column(nullable = false, length = 500)
    val title: String,

    @Column(nullable = false, length = 1000)
    val link: String,

    @Column(nullable = true, length = 2000)
    val summary: String? = null,

    @Column(nullable = true)
    val publishedAt: LocalDateTime? = null,
) : BaseEntity()
