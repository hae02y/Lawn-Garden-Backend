package org.example.lawngarden.domain.users.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity

@Entity
@Table(name = "user_level_history")
class UserLevelHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "previous_level", nullable = false)
    val previousLevel: Long,

    @Column(name = "new_level", nullable = false)
    val newLevel: Long,

    @Column(name = "post_count", nullable = false)
    val postCount: Long,
) : BaseEntity()
