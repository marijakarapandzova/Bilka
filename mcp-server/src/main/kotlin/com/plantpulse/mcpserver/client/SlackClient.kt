package com.plantpulse.mcpserver.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.plantpulse.mcpserver.config.McpProperties
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackTeam(val id: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackAuthedUser(val id: String? = null)

// Slack's Web API returns snake_case JSON regardless of this service's own naming convention.
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackOAuthResponse(
    val ok: Boolean,
    val error: String? = null,
    @JsonProperty("access_token") val accessToken: String? = null,
    val team: SlackTeam? = null,
    @JsonProperty("authed_user") val authedUser: SlackAuthedUser? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackChannel(val id: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackApiResponse(val ok: Boolean, val error: String? = null, val channel: SlackChannel? = null)

/** Thin wrapper around the Slack Web API endpoints this integration needs. */
@Component
class SlackClient(private val properties: McpProperties) {

    private val client = RestClient.create("https://slack.com/api")

    fun buildInstallUrl(state: String): String {
        val scopes = "chat:write,im:write"
        val redirect = URLEncoder.encode(properties.slack.redirectUri, StandardCharsets.UTF_8)
        return "https://slack.com/oauth/v2/authorize" +
            "?client_id=${properties.slack.clientId}" +
            "&scope=$scopes" +
            "&redirect_uri=$redirect" +
            "&state=$state"
    }

    fun exchangeCode(code: String): SlackOAuthResponse {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap()
        form.add("client_id", properties.slack.clientId)
        form.add("client_secret", properties.slack.clientSecret)
        form.add("code", code)
        form.add("redirect_uri", properties.slack.redirectUri)

        val response = client.post()
            .uri("/oauth.v2.access")
            .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(SlackOAuthResponse::class.java)
            ?: error("Slack returned an empty OAuth response")

        check(response.ok) { "Slack OAuth exchange failed: ${response.error}" }
        return response
    }

    /** Opens (or reuses) a DM with the installing user so the daily checklist has somewhere to land. */
    fun openDirectMessage(botToken: String, slackUserId: String): String {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap()
        form.add("users", slackUserId)

        val response = client.post()
            .uri("/conversations.open")
            .header("Authorization", "Bearer $botToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(SlackApiResponse::class.java)
            ?: error("Slack returned an empty conversations.open response")

        check(response.ok) { "Slack conversations.open failed: ${response.error}" }
        return response.channel?.id ?: error("Slack did not return a DM channel id")
    }

    fun postMessage(botToken: String, channelId: String, text: String) {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap()
        form.add("channel", channelId)
        form.add("text", text)

        val response = client.post()
            .uri("/chat.postMessage")
            .header("Authorization", "Bearer $botToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(SlackApiResponse::class.java)
            ?: error("Slack returned an empty chat.postMessage response")

        check(response.ok) { "Slack chat.postMessage failed: ${response.error}" }
    }
}
