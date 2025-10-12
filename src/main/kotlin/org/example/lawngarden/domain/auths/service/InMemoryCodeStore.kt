package org.example.lawngarden.domain.auths.service

import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class InMemoryCodeStore : CodeStore {

    private val store = ConcurrentHashMap<String, Pair<String, String>>()

    override fun issue(sub: String, name: String): String {
        val code = UUID.randomUUID().toString()
        store[code] = sub to name
        return code
    }

    override fun consume(code: String): Pair<String, String>? = store.remove(code)

}