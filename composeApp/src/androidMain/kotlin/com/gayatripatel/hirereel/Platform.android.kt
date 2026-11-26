package com.gayatripatel.hirereel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.io.File

actual fun platformName(): String = "Android ${Build.VERSION.RELEASE}"

/** Muted, looping ExoPlayer with short buffers; released as soon as the page leaves the pool window. */
@OptIn(UnstableApi::class)
@Composable
actual fun FeedVideo(url: String, playing: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context)
            .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(2_000, 8_000, 500, 1_000).build())
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
            }
    }
    LaunchedEffect(player, playing) { player.playWhenReady = playing }
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(Color.TRANSPARENT)
                this.player = player
            }
        },
        update = { it.player = player },
    )
}

/**
 * CameraX front-camera recorder at SD quality (with fallback), binding only Preview + VideoCapture and
 * unbinding when the interview screen leaves, to keep camera memory low.
 */
class CameraXRecorder(private val context: Context) : VideoRecorder {
    override val usesCamera = true
    private val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.SD, FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
        .build()
    private val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)
    private var recording: Recording? = null
    private var provider: ProcessCameraProvider? = null

    fun bind(owner: LifecycleOwner, view: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val cameraProvider = future.get()
            provider = cameraProvider
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
            cameraProvider.unbindAll()
            runCatching { cameraProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, videoCapture) }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("MissingPermission")
    override fun start(fileName: String, onSaved: (path: String, sizeBytes: Long) -> Unit, onError: (String) -> Unit) {
        if (recording != null) return
        val file = File(File(context.filesDir, "interviews").apply { mkdirs() }, fileName)
        recording = videoCapture.output
            .prepareRecording(context, FileOutputOptions.Builder(file).build())
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    recording = null
                    if (event.hasError()) onError("Recording failed (${event.error})") else onSaved(file.absolutePath, file.length())
                }
            }
    }

    override fun stop() {
        recording?.stop()
    }

    fun unbind() {
        recording?.close()
        recording = null
        provider?.unbindAll()
    }
}

@Composable
actual fun rememberVideoRecorder(): VideoRecorder {
    val context = LocalContext.current
    val recorder = remember { CameraXRecorder(context.applicationContext) }
    DisposableEffect(recorder) { onDispose { recorder.unbind() } }
    return recorder
}

@Composable
actual fun CameraPreview(recorder: VideoRecorder, modifier: Modifier) {
    val camera = recorder as? CameraXRecorder ?: return
    val owner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                camera.bind(owner, this)
            }
        },
    )
}
