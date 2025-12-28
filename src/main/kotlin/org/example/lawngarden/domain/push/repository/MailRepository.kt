package org.example.lawngarden.domain.push.repository

import org.example.lawngarden.domain.push.entity.Mail
import org.example.lawngarden.domain.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MailRepository : JpaRepository<Mail, Long> {
    fun findByUser(user : User): Mail?
    fun id(id: Long): MutableList<Mail>
}