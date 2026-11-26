package com.gayatripatel.hirereel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Short platform label, e.g. "Android 15", "Desktop (Linux)", "iOS 18.1". */
expect fun platformName(): String

/**
 * One page of the video feed. Android plays it with Media3 ExoPlayer; Desktop and iOS show the poster frame.
 * The shared feed decides which pages may hold a player at all (PlayerPoolPolicy), so at most three exist.
 */
@Composable
expect fun FeedVideo(url: String, playing: Boolean, modifier: Modifier)

/** Records interview answers. Android uses CameraX; Desktop and iOS use [SimulatedRecorder]. */
interface VideoRecorder {
    val usesCamera: Boolean
    fun start(fileName: String, onSaved: (path: String, sizeBytes: Long) -> Unit, onError: (String) -> Unit)
    fun stop()
}

@Composable
expect fun rememberVideoRecorder(): VideoRecorder

/** Live camera preview behind the interview controls (draws nothing where there is no camera integration). */
@Composable
expect fun CameraPreview(recorder: VideoRecorder, modifier: Modifier)

/** Stand-in recorder: produces a placeholder "file" sized like a 480p answer (~1.2 MB per second). */
class SimulatedRecorder(private val now: () -> Long) : VideoRecorder {
    override val usesCamera = false
    private var startedAt = 0L
    private var pending: ((String, Long) -> Unit)? = null
    private var name = ""

    override fun start(fileName: String, onSaved: (path: String, sizeBytes: Long) -> Unit, onError: (String) -> Unit) {
        startedAt = now()
        pending = onSaved
        name = fileName
    }

    override fun stop() {
        val callback = pending ?: return
        pending = null
        val seconds = ((now() - startedAt) / 1000).coerceAtLeast(1)
        callback("simulated/$name", seconds * 1_200_000)
    }
}
