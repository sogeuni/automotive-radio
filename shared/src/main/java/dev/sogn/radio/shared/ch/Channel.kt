package dev.sogn.radio.shared.ch

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaConstants


abstract class Channel(
    val id: String,
    val name: String,
    val station: String,
    val url: String,
) {
    @OptIn(UnstableApi::class)
    fun toMediaMetadata(): MediaMetadata = MediaMetadata.Builder().also {
        it.setTitle(name)
        it.setDisplayTitle(name)
        it.setArtist(station)
        it.setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
        it.setIsBrowsable(false)
        it.setIsPlayable(true)
        it.setExtras(
            Bundle().apply {
                putString(MediaConstants.EXTRAS_KEY_CONTENT_STYLE_GROUP_TITLE, station)
            }
        )
    }.build()

    open fun toMediaItemForBrowse(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(url)
        .setMediaMetadata(toMediaMetadata())
        .build()

    open suspend fun toMediaItemForPlay(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(url)
        .setMediaMetadata(toMediaMetadata())
        .build()
}

val channels = listOf(
    Kbs(id = "kbs_21", name = "1라디오", code = "21"),
    Kbs(id = "kbs_22", name = "2라디오", code = "22"),
    Kbs(id = "kbs_24", name = "1FM", code = "24"),
    Kbs(id = "kbs_25", name = "2FM", code = "25"),
    Kbs(id = "kbs_26", name = "한민족방송", code = "26"),
    Kbs(id = "kbs_10_21", name = "부산1라디오", code = "10_21"),
    Kbs(id = "kbs_10_22", name = "부산2라디오", code = "10_22"),
    Kbs(id = "kbs_10_24", name = "부산1FM", code = "10_24"),
    Kbs(id = "kbs_11_21", name = "울산1라디오", code = "11_21"),
    Kbs(id = "kbs_20_21", name = "창원1라디오", code = "20_21"),
    Kbs(id = "kbs_20_22", name = "창원2라디오", code = "20_22"),
    Kbs(id = "kbs_20_24", name = "창원1FM", code = "20_24"),
    Kbs(id = "kbs_21_21", name = "진주1라디오", code = "21_21"),
    Kbs(id = "kbs_30_21", name = "대구1라디오", code = "30_21"),
    Kbs(id = "kbs_30_22", name = "대구2라디오", code = "30_22"),
    Kbs(id = "kbs_30_24", name = "대구1FM", code = "30_24"),
    Kbs(id = "kbs_31_21", name = "안동1라디오", code = "31_21"),
    Kbs(id = "kbs_32_21", name = "포항1라디오", code = "32_21"),
    Kbs(id = "kbs_40_21", name = "광주1라디오", code = "40_21"),
    Kbs(id = "kbs_40_22", name = "광주2라디오", code = "40_22"),
    Kbs(id = "kbs_40_24", name = "광주1FM", code = "40_24"),
    Kbs(id = "kbs_41_21", name = "목포1라디오", code = "41_21"),
    Kbs(id = "kbs_41_24", name = "목포1FM", code = "41_24"),
    Kbs(id = "kbs_43_21", name = "순천1라디오", code = "43_21"),
    Kbs(id = "kbs_50_21", name = "전주1라디오", code = "50_21"),
    Kbs(id = "kbs_50_22", name = "전주2라디오", code = "50_22"),
    Kbs(id = "kbs_50_24", name = "전주1FM", code = "50_24"),
    Kbs(id = "kbs_60_21", name = "대전1라디오", code = "60_21"),
    Kbs(id = "kbs_60_22", name = "대전2라디오", code = "60_22"),
    Kbs(id = "kbs_60_24", name = "대전1FM", code = "60_24"),
    Kbs(id = "kbs_70_21", name = "청주1라디오", code = "70_21"),
    Kbs(id = "kbs_70_22", name = "청주2라디오", code = "70_22"),
    Kbs(id = "kbs_70_24", name = "청주1FM", code = "70_24"),
    Kbs(id = "kbs_80_21", name = "춘천1라디오", code = "80_21"),
    Kbs(id = "kbs_80_22", name = "춘천2라디오", code = "80_22"),
    Kbs(id = "kbs_80_24", name = "춘천1FM", code = "80_24"),
    Kbs(id = "kbs_81_21", name = "강릉1라디오", code = "81_21"),
    Kbs(id = "kbs_81_24", name = "강릉1FM", code = "81_24"),
    Kbs(id = "kbs_82_21", name = "원주1라디오", code = "82_21"),
    Kbs(id = "kbs_82_24", name = "원주1FM", code = "82_24"),
    Kbs(id = "kbs_90_21", name = "제주1라디오", code = "90_21"),
    Kbs(id = "kbs_90_22", name = "제주2라디오", code = "90_22"),
    Kbs(id = "kbs_90_24", name = "제주1FM", code = "90_24"),
    Mbc(id = "mbc_busan", name = "부산MBC", url = "https://stream.bsmbc.com/live/mp4:BusanMBC-LiveStream-AM/playlist.m3u8"),
    Mbc(id = "mbc_ulsan", name = "울산MBC", url = "https://5ddfd163bd00d.streamlock.net/STDFM/STDFM/playlist.m3u8"),
)







