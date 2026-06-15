package com.lucasbueno.moises_challenge.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.lucasbueno.moises_challenge.presentation.navigation.MusicNavHost
import com.lucasbueno.moises_challenge.presentation.theme.MoiseschallengeTheme

@Composable
fun RootScreen() {
    MoiseschallengeTheme {
        val navController = rememberNavController()

        MusicNavHost(navController = navController)
    }
}
