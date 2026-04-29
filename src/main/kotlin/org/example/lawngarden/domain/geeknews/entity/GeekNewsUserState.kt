package org.example.lawngarden.domain.geeknews.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDateTime

@Entity
@Table(
    name = "geek_news_user_state",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "article_id"])],
)
class GeekNewsUserState(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    val article: GeekNewsArticle,

    @Column(nullable = false)
    var bookmarked: Boolean = false,

    @Column(name = "read_at", nullable = true)
    var readAt: LocalDateTime? = null,
) : BaseEntity() {
    fun markRead(readTime: LocalDateTime) {
        readAt = readTime
    }
}
