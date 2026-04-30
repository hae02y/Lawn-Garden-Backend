package org.example.lawngarden.domain.social.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.social.enums.CheerType
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDate

@Entity
@Table(
    name = "garden_cheers",
    uniqueConstraints = [UniqueConstraint(columnNames = ["from_user_id", "to_user_id", "cheer_date"])],
)
class GardenCheer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    val toUser: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CheerType,

    @Column(name = "cheer_date", nullable = false)
    val cheerDate: LocalDate,
) : BaseEntity()
