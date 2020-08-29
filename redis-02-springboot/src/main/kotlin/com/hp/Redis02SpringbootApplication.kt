package com.hp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Redis02SpringbootApplication

fun main(args: Array<String>) {
    runApplication<Redis02SpringbootApplication>(*args)
}
