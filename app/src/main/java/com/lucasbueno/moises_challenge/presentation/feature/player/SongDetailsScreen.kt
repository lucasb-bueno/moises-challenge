package com.lucasbueno.moises_challenge.presentation.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.presentation.component.ArtworkImage
import com.lucasbueno.moises_challenge.presentation.component.CircleIconButton
import com.lucasbueno.moises_challenge.presentation.component.MoreOptionsBottomSheet
import com.lucasbueno.moises_challenge.presentation.component.PlayerControls
import com.lucasbueno.moises_challenge.presentation.component.PlayerTimeline
import com.lucasbueno.moises_challenge.presentation.component.ScreenTopBar
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData
import com.lucasbueno.moises_challenge.ui.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@Composable
fun SongDetailsScreen(
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = PreviewMusicData.songById(5L)
    var showOptions by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenTopBar(
                title = song.albumName.orEmpty(),
                navigationIcon = {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        onClick = onBackClick,
                    )
                },
                actions = {
                    CircleIconButton(
                        icon = Icons.Rounded.MoreHoriz,
                        contentDescription = "More options",
                        onClick = { showOptions = true },
                    )
                },
            )

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
                        contentDescription = "Repeat",
                        tint = MusicColors.TextPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                PlayerTimeline(
                    progress = 0.32f,
                    elapsedText = "1:26",
                    remainingText = "-2:54",
                )

                Spacer(modifier = Modifier.height(26.dp))

                PlayerControls(
                    isPlaying = false,
                    onPreviousClick = {},
                    onPlayPauseClick = {},
                    onNextClick = {},
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(34.dp))
            }
        }

        if (showOptions) {
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
private fun SongDetailsScreenPreview() {
    MoiseschallengeTheme {
        SongDetailsScreen(
            onBackClick = {},
            onAlbumClick = {},
        )
    }
}
