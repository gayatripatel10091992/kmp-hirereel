package com.gayatripatel.hirereel

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders every [DemoScenes] scene headlessly with the desktop (JVM/Skia) Compose runtime, at phone size.
 * Android and iOS render the same scenes on an emulator and a simulator in CI.
 */
@OptIn(ExperimentalComposeUiApi::class)
class ScreenshotTest {
    @Test
    fun allScenes() {
        DemoScenes.all.forEach(::capture)
    }

    private fun capture(scene: DemoScenes.Scene) {
        val density = 2.625f
        val file = File("build/screenshots/desktop/${scene.name}.png")
        val composeScene = ImageComposeScene(
            width = (411 * density).toInt(),
            height = (891 * density).toInt(),
            density = Density(density),
        ) { DemoScene(scene) }
        val image = try {
            composeScene.render()
            composeScene.render(nanoTime = 500_000_000L)
        } finally {
            composeScene.close()
        }
        val png = image.encodeToData(EncodedImageFormat.PNG) ?: error("PNG encoding failed for ${scene.name}")
        file.parentFile.mkdirs()
        file.writeBytes(png.bytes)
        assertTrue(file.length() > 0, "Screenshot ${scene.name} was not written")
    }
}
