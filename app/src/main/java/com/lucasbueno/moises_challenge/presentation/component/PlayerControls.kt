package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous song",
                tint = MusicColors.TextPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(
            onClick = onPlayPauseClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MusicColors.TextPrimary),
            modifier = Modifier
                .size(MusicDimens.PlayerButtonSize)
                .clip(CircleShape)
                .background(MusicColors.SurfaceElevated),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                modifier = Modifier.size(34.dp),
            )
        }
        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next song",
                tint = MusicColors.TextPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
