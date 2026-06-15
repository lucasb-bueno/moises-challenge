package com.lucasbueno.moises_challenge.presentation.feature.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import com.lucasbueno.moises_challenge.presentation.component.ArtworkImage
import com.lucasbueno.moises_challenge.presentation.component.CircleIconButton
import com.lucasbueno.moises_challenge.presentation.component.MoreOptionsBottomSheet
import com.lucasbueno.moises_challenge.presentation.component.ScreenStateContent
import com.lucasbueno.moises_challenge.presentation.component.SongListItem
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData
import com.lucasbueno.moises_challenge.presentation.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.presentation.theme.MusicColors
import com.lucasbueno.moises_challenge.presentation.theme.MusicDimens

@Composable
fun AlbumScreen(
    uiState: AlbumUiState,
    onBackClick: () -> Boolean,
    onSongClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val album = uiState.album
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var isBackNavigationRequested by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MusicDimens.TopBarHeight)
                    .padding(horizontal = MusicDimens.ScreenHorizontalPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
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
            }

            ScreenStateContent(
                screenState = uiState.screenState,
                onRetryClick = onRetryClick,
                modifier = Modifier.weight(1f),
            ) {
                if (album == null) return@ScreenStateContent

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MusicDimens.ScreenHorizontalPadding,
                        end = MusicDimens.ScreenHorizontalPadding,
                        bottom = MusicDimens.ScreenVerticalPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillParentMaxWidth(),
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ArtworkImage(
                                artworkUrl = album.artworkUrl,
                                contentDescription = album.name,
                                size = MusicDimens.AlbumArtworkSize,
                                shape = RoundedCornerShape(MusicDimens.ArtworkCornerRadius),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = album.name,
                                color = MusicColors.TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = album.artistName,
                                color = MusicColors.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(42.dp))
                        }
                    }
                    items(
                        items = album.songs,
                        key = Song::id,
                    ) { song ->
                        SongListItem(
                            title = song.name,
                            subtitle = song.artistName,
                            artworkUrl = song.artworkUrl,
                            onClick = { onSongClick(song.id) },
                            onMoreClick = { selectedSong = song },
                        )
                    }
                }
            }
        }

        selectedSong?.let { song ->
            MoreOptionsBottomSheet(
                title = song.name,
                subtitle = song.artistName,
                onDismissRequest = { selectedSong = null },
                onViewAlbumClick = { selectedSong = null },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AlbumScreenShowPreview() {
    MoiseschallengeTheme {
        AlbumScreen(
            uiState = PreviewMusicData.albumUiState(albumId = 5L),
            onBackClick = { true },
            onSongClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AlbumScreenLoadingPreview() {
    MoiseschallengeTheme {
        AlbumScreen(
            uiState = AlbumUiState(screenState = ScreenState.Loading),
            onBackClick = { true },
            onSongClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AlbumScreenErrorPreview() {
    MoiseschallengeTheme {
        AlbumScreen(
            uiState = AlbumUiState(
                screenState = ScreenState.Error("Unable to load album"),
            ),
            onBackClick = { true },
            onSongClick = {},
            onRetryClick = {},
        )
    }
}
