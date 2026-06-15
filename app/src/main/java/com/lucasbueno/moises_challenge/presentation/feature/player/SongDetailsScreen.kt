package com.lucasbueno.moises_challenge.presentation.feature.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import com.lucasbueno.moises_challenge.presentation.component.ArtworkImage
import com.lucasbueno.moises_challenge.presentation.component.CircleIconButton
import com.lucasbueno.moises_challenge.presentation.component.MoreOptionsBottomSheet
import com.lucasbueno.moises_challenge.presentation.component.PlayerControls
import com.lucasbueno.moises_challenge.presentation.component.PlayerTimeline
import com.lucasbueno.moises_challenge.presentation.component.ScreenTopBar
import com.lucasbueno.moises_challenge.presentation.component.ScreenStateContent
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData
import com.lucasbueno.moises_challenge.presentation.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.presentation.theme.MusicColors
import com.lucasbueno.moises_challenge.presentation.theme.MusicDimens
import kotlinx.coroutines.delay

@Composable
fun SongDetailsScreen(
    uiState: SongDetailsUiState,
    onBackClick: () -> Boolean,
    onAlbumClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    onPlaybackStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOptions by remember { mutableStateOf(false) }
    var isBackNavigationRequested by remember { mutableStateOf(false) }
    val song = uiState.song
    val isInspectionMode = LocalInspectionMode.current

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlayerReady by remember(song?.id) { mutableStateOf(false) }
    var isPlaying by remember(song?.id) { mutableStateOf(false) }
    var durationMillis by remember(song?.id) { mutableStateOf(0L) }
    var positionMillis by remember(song?.id) { mutableStateOf(0L) }
    var playbackError by remember(song?.id) { mutableStateOf<String?>(null) }
    var hasMarkedPlaybackStarted by remember(song?.id) { mutableStateOf(false) }
    val previewUrl = song?.previewUrl?.takeIf { it.isNotBlank() }

    DisposableEffect(song?.id, previewUrl, isInspectionMode) {
        isPlayerReady = false
        isPlaying = false
        durationMillis = 0L
        positionMillis = 0L
        playbackError = if (song != null && previewUrl == null && !isInspectionMode) {
            "Preview unavailable"
        } else {
            null
        }

        if (previewUrl == null || isInspectionMode) {
            mediaPlayer = null
            onDispose {}
        } else {
            var player: MediaPlayer? = null

            try {
                player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    )
                    setDataSource(previewUrl)
                    setOnPreparedListener { preparedPlayer ->
                        durationMillis = preparedPlayer.duration.coerceAtLeast(0).toLong()
                        isPlayerReady = true
                    }
                    setOnCompletionListener { completedPlayer ->
                        positionMillis = completedPlayer.duration.coerceAtLeast(0).toLong()
                        isPlaying = false
                    }
                    setOnErrorListener { _, _, _ ->
                        playbackError = "Preview unavailable"
                        isPlayerReady = false
                        isPlaying = false
                        true
                    }
                    prepareAsync()
                }

                mediaPlayer = player
            } catch (_: Exception) {
                player?.release()
                player = null
                mediaPlayer = null
                playbackError = "Preview unavailable"
            }

            onDispose {
                player?.release()
                if (mediaPlayer === player) {
                    mediaPlayer = null
                }
            }
        }
    }

    LaunchedEffect(mediaPlayer, isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                positionMillis = player.currentPosition.coerceAtLeast(0).toLong()
            }
            delay(PLAYBACK_POSITION_UPDATE_MILLIS)
        }
    }

    fun seekTo(position: Long) {
        val player = mediaPlayer ?: return
        val target = position.coerceIn(0L, durationMillis.coerceAtLeast(0L))
        player.seekTo(target.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        positionMillis = target
    }

    fun togglePlayback() {
        val player = mediaPlayer ?: return

        try {
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.start()
                isPlaying = true
                if (!hasMarkedPlaybackStarted) {
                    hasMarkedPlaybackStarted = true
                    onPlaybackStarted()
                }
            }
        } catch (_: IllegalStateException) {
            playbackError = "Preview unavailable"
            isPlaying = false
        }
    }

    val timelineDurationMillis = when {
        durationMillis > 0L -> durationMillis
        previewUrl == null -> song?.durationMillis ?: 0L
        else -> 0L
    }
    val progress = if (timelineDurationMillis > 0L) {
        positionMillis.toFloat() / timelineDurationMillis.toFloat()
    } else {
        0f
    }
    val elapsedText = formatDuration(positionMillis)
    val remainingText = "-${formatDuration((timelineDurationMillis - positionMillis).coerceAtLeast(0L))}"
    val controlsEnabled = isPlayerReady && playbackError == null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenTopBar(
                title = song?.albumName.orEmpty(),
                navigationIcon = {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        onClick = {
                            if (!isBackNavigationRequested) {
                                isBackNavigationRequested = onBackClick()
                            }
                        },
                        enabled = !isBackNavigationRequested,
                    )
                },
                actions = {
                    if (song != null) {
                        CircleIconButton(
                            icon = Icons.Rounded.MoreHoriz,
                            contentDescription = "More options",
                            onClick = { showOptions = true },
                        )
                    }
                },
            )

            ScreenStateContent(
                screenState = uiState.screenState,
                onRetryClick = onRetryClick,
                modifier = Modifier.weight(1f),
            ) {
                if (song == null) return@ScreenStateContent

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(92.dp))

                    ArtworkImage(
                        artworkUrl = song.artworkUrl,
                        contentDescription = song.name,
                        size = MusicDimens.PlayerArtworkSize,
                        shape = RoundedCornerShape(MusicDimens.LargeArtworkCornerRadius),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MusicDimens.ScreenHorizontalPadding),
                    ) {
                        Text(
                            text = song.name,
                            color = MusicColors.TextPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = song.artistName,
                                color = MusicColors.TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                tint = MusicColors.TextPrimary,
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        PlayerTimeline(
                            progress = progress,
                            elapsedText = elapsedText,
                            remainingText = remainingText,
                            enabled = controlsEnabled,
                            onProgressChange = { seekProgress ->
                                if (timelineDurationMillis > 0L) {
                                    seekTo((timelineDurationMillis * seekProgress).toLong())
                                }
                            },
                        )

                        playbackError?.let { message ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message,
                                color = MusicColors.TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        PlayerControls(
                            isPlaying = isPlaying,
                            enabled = controlsEnabled,
                            onBackwardClick = { seekTo(positionMillis - SEEK_INTERVAL_MILLIS) },
                            onPlayPauseClick = ::togglePlayback,
                            onForwardClick = { seekTo(positionMillis + SEEK_INTERVAL_MILLIS) },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )

                        Spacer(modifier = Modifier.height(34.dp))
                    }
                }
            }
        }

        if (showOptions) {
            if (song == null) return@Box

            MoreOptionsBottomSheet(
                title = song.name,
                subtitle = song.artistName,
                onDismissRequest = { showOptions = false },
                onViewAlbumClick = {
                    showOptions = false
                    song.albumId?.let(onAlbumClick)
                },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongDetailsScreenShowPreview() {
    MoiseschallengeTheme {
        SongDetailsScreen(
            uiState = PreviewMusicData.songDetailsUiState(songId = 5L),
            onBackClick = { true },
            onAlbumClick = {},
            onRetryClick = {},
            onPlaybackStarted = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongDetailsScreenLoadingPreview() {
    MoiseschallengeTheme {
        SongDetailsScreen(
            uiState = SongDetailsUiState(screenState = ScreenState.Loading),
            onBackClick = { true },
            onAlbumClick = {},
            onRetryClick = {},
            onPlaybackStarted = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongDetailsScreenErrorPreview() {
    MoiseschallengeTheme {
        SongDetailsScreen(
            uiState = SongDetailsUiState(
                screenState = ScreenState.Error("Unable to load song"),
            ),
            onBackClick = { true },
            onAlbumClick = {},
            onRetryClick = {},
            onPlaybackStarted = {},
        )
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private const val SEEK_INTERVAL_MILLIS = 10_000L
private const val PLAYBACK_POSITION_UPDATE_MILLIS = 250L
