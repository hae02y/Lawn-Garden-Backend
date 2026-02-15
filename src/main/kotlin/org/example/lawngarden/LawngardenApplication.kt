package org.example.lawngarden

import org.example.lawngarden.common.properties.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class LawngardenApplication
fun main(args: Array<String>) {
    runApplication<LawngardenApplication>(*args)
}
