package com.lucasbueno.moises_challenge.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.lucasbueno.moises_challenge.presentation.navigation.MusicNavHost
import com.lucasbueno.moises_challenge.ui.theme.MoiseschallengeTheme

@Composable
fun MoisesApp() {
    MoiseschallengeTheme {
        val navController = rememberNavController()

        MusicNavHost(navController = navController)
    }
}
