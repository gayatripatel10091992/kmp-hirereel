package com.gayatripatel.hirereel

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    val viewModel = HireReelViewModel()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HireReel",
            state = rememberWindowState(width = 430.dp, height = 880.dp),
        ) {
            App(viewModel)
        }
    }
}
