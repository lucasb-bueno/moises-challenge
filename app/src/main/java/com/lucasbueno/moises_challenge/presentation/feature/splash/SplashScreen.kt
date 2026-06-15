package com.lucasbueno.moises_challenge.presentation.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucasbueno.moises_challenge.R
import com.lucasbueno.moises_challenge.presentation.theme.MoiseschallengeTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .splashGradient(),
    ) {
        Image(
            painter = painterResource(R.drawable.musical_note),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 72.dp)
                .size(92.dp),
        )
    }
}

private fun Modifier.splashGradient(): Modifier =
    drawWithCache {
        val gradient = Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.Black,
                SPLASH_GRADIENT_BLACK_STOP to Color.Black,
                1f to SPLASH_GRADIENT_VISIBLE_END_COLOR,
            ),
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
        )

        onDrawBehind {
            drawRect(gradient)
        }
    }

private const val SPLASH_GRADIENT_BLACK_STOP = 0.3357f
private val SPLASH_GRADIENT_VISIBLE_END_COLOR = Color(0xFF00343E)

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MoiseschallengeTheme {
        SplashScreen()
    }
}
