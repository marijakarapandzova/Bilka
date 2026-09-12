package com.plantpulse.mcpserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "plantpulse")
class McpProperties {
    lateinit var plantServiceUrl: String
    lateinit var healthServiceUrl: String
    lateinit var reminderCron: String
    lateinit var tokenEncryptionKey: String
    val slack = Slack()
    val google = Google()

    class Slack {
        var clientId: String = ""
        var clientSecret: String = ""
        lateinit var redirectUri: String
    }

    class Google {
        var clientId: String = ""
        var clientSecret: String = ""
        lateinit var redirectUri: String
    }
}
