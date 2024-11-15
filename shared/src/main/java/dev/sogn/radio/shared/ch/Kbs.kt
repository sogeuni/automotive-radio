package dev.sogn.radio.shared.ch

import android.util.Log
import androidx.media3.common.MediaItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting

class Kbs(id: String, name: String, code: String) : Channel(id, name, "KBS", BASE_URL + code) {

    companion object {
        private const val BASE_URL = "https://cfpwwwapi.kbs.co.kr/api/v1/landing/live/channel_code/"

        private val kbsClient = HttpClient(CIO) {
            defaultRequest {
                // base url
                url(BASE_URL)
            }
            install(ContentNegotiation) {
                json(Json {
                    // JSON에서 알 수 없는 속성이 발견될 때 SerializationException 대신 무시함
                    ignoreUnknownKeys = true
                })
            }
        }

        @VisibleForTesting
        suspend fun printKbsSupportedChannel() {
            withContext(Dispatchers.IO) {
                for (city in 0..99) {
                    for (ch in 21..26) {
                        val code =
                            if (city == 0) "$ch" else "${city.toString().padStart(2, '0')}_$ch"
                        val response: HttpResponse = kbsClient.get(code)

                        if (response.status == HttpStatusCode.OK) {
                            val channel = response.body<KbsChannel>()
                            if (channel.channelItem.isNotEmpty() && channel.channelMaster != null) {
                                Log.e(
                                    "KBS",
                                    "${channel.channelMaster.code} ${channel.channelMaster.title} ${channel.channelItem[0].url}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun toMediaItemForBrowse(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setMediaMetadata(toMediaMetadata())
        .build()

    override suspend fun toMediaItemForPlay(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(getPlayUrl())
        .setMediaMetadata(toMediaMetadata())
        .build()

    private suspend fun getPlayUrl(): String? {
        return kbsClient.get(url).let { response ->
            if (response.status != HttpStatusCode.OK) {
                null
            } else {
                val channel = response.body<KbsChannel>()
                if (channel.channelItem.isEmpty() || channel.channelMaster == null) {
                    null
                } else {
                    channel.channelItem[0].url
                }
            }
        }
    }
}

@Serializable
data class KbsChannel(
    val ret: Int,
    val msg: String,
    @SerialName("channel_item")
    val channelItem: List<ChannelItem>,
    val channelMaster: ChannelMaster?,
)

@Serializable
data class ChannelItem(
    @SerialName("service_url")
    val url: String,
)

@Serializable
data class ChannelMaster(
    @SerialName("channel_code")
    val code: String,
    val title: String,
)


