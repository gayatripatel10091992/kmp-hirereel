package com.gayatripatel.hirereel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

actual fun platformName(): String = "${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}"

/** iOS shows the poster frame; an AVPlayerLayer in a UIKitView is the native drop-in. */
@Composable
actual fun FeedVideo(url: String, playing: Boolean, modifier: Modifier) = Unit

@Composable
actual fun rememberVideoRecorder(): VideoRecorder =
    remember { SimulatedRecorder { (NSDate().timeIntervalSince1970 * 1000).toLong() } }

@Composable
actual fun CameraPreview(recorder: VideoRecorder, modifier: Modifier) = Unit
