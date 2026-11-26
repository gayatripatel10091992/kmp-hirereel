package com.gayatripatel.hirereel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

actual fun platformName(): String = "Desktop (${System.getProperty("os.name")})"

/** Desktop shows the poster frame; a VLCJ or JavaFX player could be plugged in here. */
@Composable
actual fun FeedVideo(url: String, playing: Boolean, modifier: Modifier) = Unit

@Composable
actual fun rememberVideoRecorder(): VideoRecorder = remember { SimulatedRecorder { System.currentTimeMillis() } }

@Composable
actual fun CameraPreview(recorder: VideoRecorder, modifier: Modifier) = Unit
