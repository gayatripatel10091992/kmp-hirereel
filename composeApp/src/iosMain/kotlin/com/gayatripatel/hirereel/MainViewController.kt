package com.gayatripatel.hirereel

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Entry point for the iOS host app: `MainViewControllerKt.MainViewController(scene:)` from Swift.
 * With a scene name (launch argument `-scene 03_jobs`, used by CI on the simulator) it shows that README screen with
 * demo data; otherwise the app.
 */
fun MainViewController(scene: String?): UIViewController {
    val demo = DemoScenes.named(scene)
    if (demo != null) return ComposeUIViewController { DemoScene(demo) }
    val viewModel = HireReelViewModel()
    return ComposeUIViewController { App(viewModel) }
}
