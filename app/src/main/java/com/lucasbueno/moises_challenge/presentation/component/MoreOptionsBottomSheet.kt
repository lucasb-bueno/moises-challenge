package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Song name",
    subtitle: String = "Artist name",
    onViewAlbumClick: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = MusicColors.SurfaceElevated,
        contentColor = MusicColors.TextPrimary,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MusicColors.IconMuted)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MusicColors.SurfaceElevated)
                .padding(bottom = MusicDimens.ScreenVerticalPadding),
        ) {
            Text(
                text = title,
                color = MusicColors.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Text(
                text = subtitle,
                color = MusicColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "View album",
                        color = MusicColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null,
                        tint = MusicColors.TextPrimary,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(top = 18.dp)
                    .clickable(onClick = onViewAlbumClick),
            )
        }
    }
}
