package org.example.lawngarden.domain.auths.service

interface CodeStore {
    fun issue(sub: String, name: String): String
    fun consume(code: String): Pair<String, String>?
}