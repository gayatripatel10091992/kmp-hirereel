import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            // Edge to edge: the shared Compose UI applies the safe-area insets itself.
            ComposeView().ignoresSafeArea()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // `-scene 03_jobs` on the launch command line lands in UserDefaults; CI uses it to open one README screen.
        MainViewControllerKt.MainViewController(scene: UserDefaults.standard.string(forKey: "scene"))
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
