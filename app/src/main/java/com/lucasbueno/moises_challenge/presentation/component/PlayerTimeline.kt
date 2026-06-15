package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.presentation.theme.MusicColors

@Composable
fun PlayerTimeline(
    progress: Float,
    elapsedText: String,
    remainingText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onProgressChange: ((Float) -> Unit)? = null,
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val isSeekEnabled = enabled && onProgressChange != null

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
        Slider(
            value = safeProgress,
            onValueChange = { value ->
                onProgressChange?.invoke(value.coerceIn(0f, 1f))
            },
            enabled = isSeekEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MusicColors.TextPrimary,
                activeTrackColor = MusicColors.TextPrimary,
                inactiveTrackColor = MusicColors.SurfaceElevated,
                disabledThumbColor = MusicColors.TextPrimary,
                disabledActiveTrackColor = MusicColors.TextPrimary,
                disabledInactiveTrackColor = MusicColors.SurfaceElevated,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        )
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
