package dev.sogn.radio.shared

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.sogn.radio.shared.ch.channels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future

class RadioService : MediaLibraryService() {

    private val context: Context = this

    private lateinit var player: ExoPlayer

    private var mediaLibrarySession: MediaLibrarySession? = null

    private val job = SupervisorJob()

    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val callback = object : MediaLibrarySession.Callback {


        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            Log.d(TAG, "onConnect: $controller")

            val sessionCommands = ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(SessionCommand(COMMAND_TEST, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_TEST2, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_TEST3, Bundle.EMPTY))
                .remove(SessionCommand.COMMAND_CODE_LIBRARY_SEARCH)
                .build()

            val playerCommands = ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .build()

            val favoriteButton = CommandButton.Builder(CommandButton.ICON_ALBUM)
                .setDisplayName("Save to favorites")
                .setSessionCommand(SessionCommand(COMMAND_TEST, Bundle()))
                .build()

            val likeButton = CommandButton.Builder(CommandButton.ICON_RADIO)
                .setDisplayName("Like")
                .setSessionCommand(
                    SessionCommand(COMMAND_TEST2, Bundle.EMPTY)
                )
                .build()

            val likeButton2 = CommandButton.Builder(CommandButton.ICON_MINUS)
                .setDisplayName("Like2")
                .setSessionCommand(
                    SessionCommand(COMMAND_TEST3, Bundle.EMPTY)
                )
                .build()

            return ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .setCustomLayout(listOf(favoriteButton, likeButton, likeButton2))
                .build()
        }

        override fun onPostConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            Log.d(TAG, "onPostConnect")
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            Log.d(TAG, "onDisconnected")
        }

        @OptIn(UnstableApi::class)
        val libraryParams = LibraryParams
            .Builder()
            .setExtras(Bundle().apply {
                putBoolean(
                    MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_PREV,
                    true
                )
                putBoolean(
                    MediaConstants.EXTRAS_KEY_SLOT_RESERVATION_SEEK_TO_NEXT,
                    false
                )
                putInt(
                    MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                    MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                )
                putInt(
                    MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                    MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                )
            })
            .build()

        @OptIn(UnstableApi::class)
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "onGetLibraryRoot")
            params?.log()

            val root = MediaItem.Builder().apply {
                setMediaId(BROWSABLE_ROOT)
                setMediaMetadata(
                    MediaMetadata.Builder().setIsBrowsable(true).setIsPlayable(false).build()
                )
            }.build()

            return Futures.immediateFuture(LibraryResult.ofItem(root, libraryParams))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.v(TAG, "onGetChildren: parentId=$parentId page=$page/$pageSize")

            return scope.future {
                getResultAsync(parentId, params)
            }
        }

        @OptIn(UnstableApi::class)
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.e(TAG, "onPlaybackResumption")
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {

            Log.e(TAG, "onAddMediaItems ${mediaItems.size}")

            return scope.future {
                channels.find {
                    mediaItems[0].mediaId == it.id
                }?.let { ch ->
                    mutableListOf(ch.toMediaItemForPlay())
                } ?: run {
                    mutableListOf()
                }
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.e(TAG, "onCustomCommand ${customCommand.customAction}")
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        @OptIn(UnstableApi::class)
        fun getResultAsync(
            parentId: String,
            params: LibraryParams? = null
        ): LibraryResult<ImmutableList<MediaItem>> {
            return if (parentId == BROWSABLE_ROOT) {
                val rootCategory = MediaMetadata.Builder().apply {
                    setTitle("1111")
                    setArtworkUri(
                        Uri.parse(
                            RESOURCE_ROOT_URI +
                                    context.resources.getResourceEntryName(R.drawable.ic_radio)
                        )
                    )
                    setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
                    setIsBrowsable(true)
                    setIsPlayable(false)
                }.build()
                val root = MediaItem.Builder().apply {
                    setMediaId("station")
                    setMediaMetadata(rootCategory)
                }.build()

//                val root2 = MediaItem.Builder().apply {
//                    setMediaId("2222")
//                    setMediaMetadata(rootCategory)
//                }.build()

                LibraryResult.ofItemList(listOf(root), params)
            } else {

                // station
                LibraryResult.ofItemList(channels.map { ch ->
                    ch.toMediaItemForBrowse()
                }, params)
            }
        }
    }

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            availableCommands
            addListener(object : Player.Listener {

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    Log.d(TAG, "onMediaMetadataChanged $mediaMetadata")
                }
            })
        }
        player.addAnalyticsListener(EventLogger("player"))

        mediaLibrarySession = MediaLibrarySession
            .Builder(this, player, callback)
            .setId(packageName)
            .build()
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        Log.d(TAG, "onGetSession $mediaLibrarySession")
        return mediaLibrarySession
    }
}

const val BROWSABLE_ROOT = "/"

const val RESOURCE_ROOT_URI = "android.resource://dev.sogn.radio/drawable/"

const val COMMAND_TEST = "command_test"
const val COMMAND_TEST2 = "command_test2"
const val COMMAND_TEST3 = "command_test3"

@OptIn(UnstableApi::class)
fun LibraryParams.log() =
    Log.v(TAG, "isRecent=$isRecent, isOffline=$isOffline, isSuggested=$isSuggested, extras=$extras")