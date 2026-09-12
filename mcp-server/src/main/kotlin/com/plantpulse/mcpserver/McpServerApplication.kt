package com.plantpulse.mcpserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class McpServerApplication

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}
