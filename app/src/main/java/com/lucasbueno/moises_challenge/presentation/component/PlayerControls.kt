package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.presentation.theme.MusicColors
import com.lucasbueno.moises_challenge.presentation.theme.MusicDimens

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onBackwardClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val iconTint = if (enabled) MusicColors.TextPrimary else MusicColors.IconMuted

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackwardClick,
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay10,
                contentDescription = "Seek backward 10 seconds",
                tint = iconTint,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(
            onClick = onPlayPauseClick,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MusicColors.TextPrimary),
            modifier = Modifier
                .size(MusicDimens.PlayerButtonSize)
                .clip(CircleShape)
                .background(MusicColors.SurfaceElevated),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                tint = iconTint,
                modifier = Modifier.size(34.dp),
            )
        }
        IconButton(
            onClick = onForwardClick,
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Rounded.Forward10,
                contentDescription = "Seek forward 10 seconds",
                tint = iconTint,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
