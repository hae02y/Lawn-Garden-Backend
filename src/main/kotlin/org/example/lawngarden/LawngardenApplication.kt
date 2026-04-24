package org.example.lawngarden

import org.example.lawngarden.common.properties.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtProperties::class)
class LawngardenApplication
fun main(args: Array<String>) {
    runApplication<LawngardenApplication>(*args)
}
