package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@Composable
fun PlayerTimeline(
    progress: Float,
    elapsedText: String,
    remainingText: String,
    modifier: Modifier = Modifier,
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Playback progress"
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = safeProgress,
                    range = 0f..1f,
                )
                stateDescription = "$elapsedText elapsed, $remainingText remaining"
            },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MusicColors.SurfaceElevated),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(safeProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MusicColors.TextPrimary),
            )
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - MusicDimens.TimelineThumbSize) * safeProgress)
                    .size(MusicDimens.TimelineThumbSize)
                    .clip(CircleShape)
                    .background(MusicColors.TextPrimary),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = elapsedText,
                color = MusicColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = remainingText,
                color = MusicColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
