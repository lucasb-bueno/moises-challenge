package com.lucasbueno.moises_challenge.presentation.feature.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SongsScreen(
    onSongClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Songs")
        Button(onClick = { onSongClick(DEFAULT_SONG_ID) }) {
            Text(text = "Open song")
        }
        Button(onClick = { onAlbumClick(DEFAULT_ALBUM_ID) }) {
            Text(text = "Open album")
        }
    }
}

private const val DEFAULT_SONG_ID = 1L
private const val DEFAULT_ALBUM_ID = 1L
