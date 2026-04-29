package org.example.lawngarden.domain.geeknews.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity

@Entity
@Table(name = "geek_news_sync_logs")
class GeekNewsSyncLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val requestedLimit: Int,

    @Column(nullable = false)
    val insertedCount: Int,

    @Column(nullable = false)
    val success: Boolean,

    @Column(nullable = false, length = 1000)
    val message: String,
) : BaseEntity()
