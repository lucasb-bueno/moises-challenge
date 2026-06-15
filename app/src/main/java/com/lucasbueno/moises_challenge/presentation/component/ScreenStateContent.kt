package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lucasbueno.moises_challenge.presentation.common.ScreenState
import com.lucasbueno.moises_challenge.ui.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.ui.theme.MusicColors
import com.lucasbueno.moises_challenge.ui.theme.MusicDimens

@Composable
fun ScreenStateContent(
    screenState: ScreenState,
    modifier: Modifier = Modifier,
    errorFallbackMessage: String = "Something went wrong",
    onRetryClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    when (screenState) {
        ScreenState.Show -> content()
        ScreenState.Loading -> LoadingState(modifier = modifier)
        is ScreenState.Error -> ErrorState(
            message = screenState.message ?: errorFallbackMessage,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Loading"
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MusicColors.TextPrimary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MusicDimens.ScreenHorizontalPadding)
            .semantics {
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MusicColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        if (onRetryClick != null) {
            TextButton(onClick = onRetryClick) {
                Text(
                    text = "Try again",
                    color = MusicColors.TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenStateContentLoadingPreview() {
    MoiseschallengeTheme {
        ScreenStateContent(
            screenState = ScreenState.Loading,
            content = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenStateContentErrorPreview() {
    MoiseschallengeTheme {
        ScreenStateContent(
            screenState = ScreenState.Error("Unable to load songs"),
            onRetryClick = {},
            content = {},
        )
    }
}
