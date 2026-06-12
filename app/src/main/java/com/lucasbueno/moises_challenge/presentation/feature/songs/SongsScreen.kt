package com.lucasbueno.moises_challenge.presentation.feature.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.presentation.component.MoreOptionsBottomSheet
import com.lucasbueno.moises_challenge.presentation.component.MusicSearchField
import com.lucasbueno.moises_challenge.presentation.component.SongListItem
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData
import com.lucasbueno.moises_challenge.ui.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@Composable
fun SongsScreen(
    onSongClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val songs = PreviewMusicData.songs

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = MusicDimens.ScreenHorizontalPadding,
                top = 52.dp,
                end = MusicDimens.ScreenHorizontalPadding,
                bottom = MusicDimens.ScreenVerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Text(
                    text = "Songs",
                    color = MusicColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                MusicSearchField(
                    value = query,
                    onValueChange = { query = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(
                items = songs,
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

        selectedSong?.let { song ->
            MoreOptionsBottomSheet(
                title = song.name,
                subtitle = song.artistName,
                onDismissRequest = { selectedSong = null },
                onViewAlbumClick = {
                    selectedSong = null
                    song.albumId?.let(onAlbumClick)
                },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongsScreenPreview() {
    MoiseschallengeTheme {
        SongsScreen(
            onSongClick = {},
            onAlbumClick = {},
        )
    }
}
