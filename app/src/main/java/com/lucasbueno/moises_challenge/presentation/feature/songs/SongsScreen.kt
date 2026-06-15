package com.lucasbueno.moises_challenge.presentation.feature.songs

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.domain.model.Song
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import com.lucasbueno.moises_challenge.presentation.component.MoreOptionsBottomSheet
import com.lucasbueno.moises_challenge.presentation.component.MusicSearchField
import com.lucasbueno.moises_challenge.presentation.component.ScreenStateContent
import com.lucasbueno.moises_challenge.presentation.component.SongListItem
import com.lucasbueno.moises_challenge.presentation.mock.PreviewMusicData
import com.lucasbueno.moises_challenge.ui.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun SongsScreen(
    uiState: SongsUiState,
    onQueryChanged: (String) -> Unit,
    onSongClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    val isSearching = uiState.query.isNotBlank()
    val songs = if (isSearching) uiState.searchResults else uiState.recentlyPlayedSongs
    val listState = if (isSearching) uiState.searchResultsState else uiState.recentlyPlayedState
    val lazyListState = rememberLazyListState()

    LaunchedEffect(
        lazyListState,
        isSearching,
        uiState.query,
        songs.size,
        uiState.isLoadingNextPage,
        uiState.hasReachedSearchEnd,
    ) {
        if (
            !isSearching ||
            songs.isEmpty() ||
            uiState.isLoadingNextPage ||
            uiState.hasReachedSearchEnd
        ) {
            return@LaunchedEffect
        }

        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { lastVisibleIndex ->
                lastVisibleIndex != null &&
                    lastVisibleIndex >= songs.lastIndex - PAGINATION_PREFETCH_THRESHOLD
            }
            .distinctUntilChanged()
            .filter { shouldLoadNextPage -> shouldLoadNextPage }
            .collect {
                onLoadNextPage()
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Songs",
                color = MusicColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MusicDimens.ScreenHorizontalPadding,
                        top = 52.dp,
                        end = MusicDimens.ScreenHorizontalPadding,
                    ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            MusicSearchField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.padding(horizontal = MusicDimens.ScreenHorizontalPadding),
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScreenStateContent(
                screenState = listState,
                modifier = Modifier.fillMaxSize(),
                onRetryClick = onRetryClick,
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = MusicDimens.ScreenHorizontalPadding,
                        end = MusicDimens.ScreenHorizontalPadding,
                        bottom = MusicDimens.ScreenVerticalPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
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

                    if (uiState.isLoadingNextPage) {
                        item(key = "search-loading-next-page") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = MusicColors.TextPrimary)
                            }
                        }
                    }
                }
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

private const val PAGINATION_PREFETCH_THRESHOLD = 3

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongsScreenDefaultPreview() {
    MoiseschallengeTheme {
        SongsScreen(
            uiState = PreviewMusicData.songsUiState(query = ""),
            onQueryChanged = {},
            onSongClick = {},
            onAlbumClick = {},
            onLoadNextPage = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongsScreenSearchPreview() {
    MoiseschallengeTheme {
        SongsScreen(
            uiState = PreviewMusicData.songsUiState(query = "daft"),
            onQueryChanged = {},
            onSongClick = {},
            onAlbumClick = {},
            onLoadNextPage = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongsScreenRecentlyPlayedLoadingPreview() {
    MoiseschallengeTheme {
        SongsScreen(
            uiState = SongsUiState(
                recentlyPlayedState = ScreenState.Loading,
            ),
            onQueryChanged = {},
            onSongClick = {},
            onAlbumClick = {},
            onLoadNextPage = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SongsScreenSearchErrorPreview() {
    MoiseschallengeTheme {
        SongsScreen(
            uiState = PreviewMusicData.songsUiState(query = "nothing").copy(
                searchResultsState = ScreenState.Error("Unable to search songs"),
            ),
            onQueryChanged = {},
            onSongClick = {},
            onAlbumClick = {},
            onLoadNextPage = {},
            onRetryClick = {},
        )
    }
}
