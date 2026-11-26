package com.gayatripatel.hirereel

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point for the iOS host app: `MainViewControllerKt.MainViewController()` from Swift. */
fun MainViewController(): UIViewController {
    val viewModel = HireReelViewModel()
    return ComposeUIViewController { App(viewModel) }
}
