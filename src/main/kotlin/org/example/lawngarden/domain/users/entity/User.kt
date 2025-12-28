package org.example.lawngarden.domain.users.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.posts.entity.Post
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    private var password: String,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var post: MutableList<Post>?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private var role: Role,

    @Column(name = "likes")
    var like: String? = null,

    @Column(name = "level")
    var level: String? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "type")
    var type: String? = null,

) {
    fun getPassword(): String = password
    fun getRole(): Role = role

}