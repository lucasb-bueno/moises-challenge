package com.lucasbueno.moises_challenge.presentation.feature.player

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
fun SongDetailsScreen(
    onBackClick: () -> Unit,
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
        Text(text = "Song details")
        Button(onClick = { onAlbumClick(DEFAULT_ALBUM_ID) }) {
            Text(text = "Open album")
        }
        Button(onClick = onBackClick) {
            Text(text = "Back")
        }
    }
}

private const val DEFAULT_ALBUM_ID = 1L
