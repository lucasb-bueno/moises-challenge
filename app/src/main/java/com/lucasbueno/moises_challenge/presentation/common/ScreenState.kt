package com.lucasbueno.moises_challenge.presentation.common

sealed class ScreenState {
    data object Show : ScreenState()
    data object Loading : ScreenState()
    data class Error(val message: String?) : ScreenState()
}
