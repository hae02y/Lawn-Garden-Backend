package org.example.lawngarden.domain.users.repository

import org.example.lawngarden.domain.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean
    fun existsUserByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
    override fun findAll() : List<User>

    fun findAllByPostCreatedDate(createdDate: LocalDate): List<User>

    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT p.user.id FROM Post p
        WHERE p.createdDate = :today
    )
""")
    fun findUsersWithoutPostToday(@Param("today") today: LocalDate): List<User>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        UPDATE users u
        LEFT JOIN (
            SELECT p.user_id AS user_id, COUNT(*) AS post_count
            FROM posts p
            GROUP BY p.user_id
        ) pc ON pc.user_id = u.id
        SET u.level = CASE
            WHEN COALESCE(pc.post_count, 0) >= 60 THEN 5
            WHEN COALESCE(pc.post_count, 0) >= 30 THEN 4
            WHEN COALESCE(pc.post_count, 0) >= 15 THEN 3
            WHEN COALESCE(pc.post_count, 0) >= 7 THEN 2
            ELSE 1
        END
        """,
        nativeQuery = true
    )
    fun syncLevelByPostCount(): Int
}
