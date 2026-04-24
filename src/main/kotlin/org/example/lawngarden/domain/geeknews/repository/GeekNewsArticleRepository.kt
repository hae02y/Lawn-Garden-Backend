package org.example.lawngarden.domain.geeknews.repository

import org.example.lawngarden.domain.geeknews.entity.GeekNewsArticle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GeekNewsArticleRepository : JpaRepository<GeekNewsArticle, Long> {
    fun existsBySourceId(sourceId: String): Boolean
    fun findAllByOrderByPublishedAtDescIdDesc(pageable: Pageable): Page<GeekNewsArticle>
    fun findAllByTitleContainingIgnoreCaseOrderByPublishedAtDescIdDesc(keyword: String, pageable: Pageable): Page<GeekNewsArticle>
}
