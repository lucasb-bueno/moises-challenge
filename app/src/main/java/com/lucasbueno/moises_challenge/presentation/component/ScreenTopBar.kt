package com.lucasbueno.moises_challenge.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.lucasbueno.moises_challenge.presentation.theme.MoiseschallengeTheme
import com.lucasbueno.moises_challenge.presentation.theme.MusicColors
import com.lucasbueno.moises_challenge.presentation.theme.MusicDimens

@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MusicDimens.TopBarHeight)
            .padding(horizontal = MusicDimens.ScreenHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.invoke()
        }
        Text(
            text = title,
            color = MusicColors.TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = MusicDimens.IconButtonSize),
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions?.invoke()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenTopBarTitlePreview() {
    MoiseschallengeTheme {
        ScreenTopBar(title = "Songs")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenTopBarNavigationPreview() {
    MoiseschallengeTheme {
        ScreenTopBar(
            title = "Album title",
            navigationIcon = {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = {},
                )
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ScreenTopBarNavigationAndActionPreview() {
    MoiseschallengeTheme {
        ScreenTopBar(
            title = "Album title",
            navigationIcon = {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = {},
                )
            },
            actions = {
                CircleIconButton(
                    icon = Icons.Rounded.MoreHoriz,
                    contentDescription = "More options",
                    onClick = {},
                )
            },
        )
    }
}
