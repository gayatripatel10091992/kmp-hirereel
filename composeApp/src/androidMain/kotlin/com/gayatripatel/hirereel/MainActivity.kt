package com.gayatripatel.hirereel

import android.Manifest
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: HireReelViewModel by viewModels()

    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Debug builds only: `adb shell am start ... --es scene 03_jobs` opens one README screen with demo data (used by CI).
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val scene = if (debuggable) DemoScenes.named(intent.getStringExtra("scene")) else null
        if (scene != null) {
            val bars = if (scene.darkBars) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            setContent { DemoScene(scene) }
            return
        }

        enableEdgeToEdge()
        // Interview recording needs both; asked up front to keep the shared UI free of platform permission code.
        if (savedInstanceState == null) permissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        setContent { App(viewModel) }
    }
}
