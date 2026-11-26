# HireReel · Kotlin Multiplatform

[![CI](https://github.com/gayatripatel10091992/kmp-hirereel/actions/workflows/ci.yml/badge.svg)](https://github.com/gayatripatel10091992/kmp-hirereel/actions/workflows/ci.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.7-4285F4)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS%20%7C%20Desktop-555)

The video-hiring app from [hirereel-video-jobs](https://github.com/gayatripatel10091992/hirereel-video-jobs), rebuilt with
**Kotlin Multiplatform**. Business logic and UI are written once and run on **Android, iOS and Desktop**. Only video
playback and the camera are platform code (`expect`/`actual`).

Candidates swipe through recruiter videos, record a timed one-way video interview with retakes, and submit.
Answers upload in resumable chunks with retry and backoff.

> Portfolio project, modelled on the kind of hiring app I built at Siyana. Original code; companies are fictional,
> and the feed clips are Google's public sample videos.

## Screenshots

The same Compose Multiplatform screens on each platform, with the same demo data ([`DemoScenes`](composeApp/src/commonMain/kotlin/com/gayatripatel/hirereel/DemoScenes.kt)).
CI renders them and commits the images on every push:

- **Android**: debug APK on an API 34 emulator, each screen opened with `--es scene <name>`.
- **iOS**: the `iosApp` host built with Xcode and run on an iPhone simulator, launched with `-scene <name>`.
- **Desktop**: rendered headlessly with the desktop (JVM/Skia) runtime at phone size.

Video and camera are platform views, so poster frames stand in for them.

| | Video feed | Search | Recording | Uploads |
|---|---|---|---|---|
| **Android** | <img src="docs/screenshots/android/02_feed.png" width="160"/> | <img src="docs/screenshots/android/03_jobs.png" width="160"/> | <img src="docs/screenshots/android/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/android/10_applications.png" width="160"/> |
| **iOS** | <img src="docs/screenshots/ios/02_feed.png" width="160"/> | <img src="docs/screenshots/ios/03_jobs.png" width="160"/> | <img src="docs/screenshots/ios/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/ios/10_applications.png" width="160"/> |
| **Desktop** | <img src="docs/screenshots/desktop/02_feed.png" width="160"/> | <img src="docs/screenshots/desktop/03_jobs.png" width="160"/> | <img src="docs/screenshots/desktop/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/desktop/10_applications.png" width="160"/> |

<details open>
<summary><b>Android</b>: all screens</summary>

| Sign in | Video feed | Search | Filters (dark) | Job detail |
|---|---|---|---|---|
| <img src="docs/screenshots/android/01_login.png" width="160"/> | <img src="docs/screenshots/android/02_feed.png" width="160"/> | <img src="docs/screenshots/android/03_jobs.png" width="160"/> | <img src="docs/screenshots/android/04_jobs_filtered_dark.png" width="160"/> | <img src="docs/screenshots/android/05_job_detail.png" width="160"/> |

| Interview: ready | Recording | Review & retake | All answers | Uploads |
|---|---|---|---|---|
| <img src="docs/screenshots/android/06_interview_ready.png" width="160"/> | <img src="docs/screenshots/android/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/android/08_interview_review.png" width="160"/> | <img src="docs/screenshots/android/09_interview_finished.png" width="160"/> | <img src="docs/screenshots/android/10_applications.png" width="160"/> |

</details>

<details>
<summary><b>iOS</b>: all screens</summary>

| Sign in | Video feed | Search | Filters (dark) | Job detail |
|---|---|---|---|---|
| <img src="docs/screenshots/ios/01_login.png" width="160"/> | <img src="docs/screenshots/ios/02_feed.png" width="160"/> | <img src="docs/screenshots/ios/03_jobs.png" width="160"/> | <img src="docs/screenshots/ios/04_jobs_filtered_dark.png" width="160"/> | <img src="docs/screenshots/ios/05_job_detail.png" width="160"/> |

| Interview: ready | Recording | Review & retake | All answers | Uploads |
|---|---|---|---|---|
| <img src="docs/screenshots/ios/06_interview_ready.png" width="160"/> | <img src="docs/screenshots/ios/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/ios/08_interview_review.png" width="160"/> | <img src="docs/screenshots/ios/09_interview_finished.png" width="160"/> | <img src="docs/screenshots/ios/10_applications.png" width="160"/> |

</details>

<details>
<summary><b>Desktop</b>: all screens</summary>

| Sign in | Video feed | Search | Filters (dark) | Job detail |
|---|---|---|---|---|
| <img src="docs/screenshots/desktop/01_login.png" width="160"/> | <img src="docs/screenshots/desktop/02_feed.png" width="160"/> | <img src="docs/screenshots/desktop/03_jobs.png" width="160"/> | <img src="docs/screenshots/desktop/04_jobs_filtered_dark.png" width="160"/> | <img src="docs/screenshots/desktop/05_job_detail.png" width="160"/> |

| Interview: ready | Recording | Review & retake | All answers | Uploads |
|---|---|---|---|---|
| <img src="docs/screenshots/desktop/06_interview_ready.png" width="160"/> | <img src="docs/screenshots/desktop/07_interview_recording.png" width="160"/> | <img src="docs/screenshots/desktop/08_interview_review.png" width="160"/> | <img src="docs/screenshots/desktop/09_interview_finished.png" width="160"/> | <img src="docs/screenshots/desktop/10_applications.png" width="160"/> |

</details>

## What's shared and what's native

```mermaid
graph TD
    subgraph shared [":shared · commonMain (Android, iOS, JVM)"]
        domain["Job · InterviewQuestion · JobApplication"]
        interview["InterviewSession<br/>countdown · timed answer · retakes"]
        pool["PlayerPoolPolicy<br/>≤ 3 players in the feed"]
        upload["UploadQueue · ChunkPlanner · RetryPolicy<br/>resumable chunked upload"]
        repos["JobRepository · ApplicationRepository · JobFilter · OtpParser"]
    end
    subgraph ui [":composeApp · commonMain"]
        vm["HireReelViewModel (multiplatform lifecycle)"]
        screens["Feed · Jobs · Job detail · Interview · Applications · Sign in"]
        expect["expect FeedVideo · rememberVideoRecorder · CameraPreview"]
    end
    android["androidMain<br/>Media3 ExoPlayer · CameraX"] --> expect
    ios["iosMain<br/>ComposeUIViewController"] --> expect
    desktop["desktopMain<br/>Compose Window"] --> expect
    screens --> vm --> shared
```

| Platform | Entry point | Video / camera |
|---|---|---|
| Android | `MainActivity` | Media3 ExoPlayer (muted, looping, short buffers) and CameraX front-camera recording at SD |
| iOS | `MainViewController(scene:)` in the `iosApp` host (SwiftUI, XcodeGen spec) | Poster frames and a simulated recorder (AVPlayer and AVFoundation are the native drop-ins) |
| Desktop | `main.kt` (`./gradlew :composeApp:run`) | Poster frames and a simulated recorder |

## Tests

- **`:shared` commonTest** (runs on the JVM, Android unit tests and the iOS simulator): pool policy, interview flow and
  retake limits, chunking and backoff, filters, formatting, OTP parsing, and `UploadQueue` with virtual time (success,
  retry on 503, stop on 413).
- **Screenshots**: `DemoScenes` holds every README screen; the desktop `ScreenshotTest`, the Android emulator and the
  iOS simulator each render them.
- **CI**: Ubuntu runs the shared tests, builds the Android APK and desktop distributable and renders the desktop
  screenshots; a second Ubuntu job captures Android on an emulator; macOS runs the shared tests on the iOS simulator,
  builds `iosApp` and captures iOS.

## Build

```bash
./gradlew :shared:desktopTest           # shared logic on the JVM
./gradlew :composeApp:assembleDebug     # Android APK
./gradlew :composeApp:run               # Desktop app
./gradlew :composeApp:desktopTest       # desktop screenshots → composeApp/build/screenshots/desktop

# iOS (macOS): generate the Xcode project, then build and run it from Xcode
brew install xcodegen && (cd iosApp && xcodegen generate) && open iosApp/iosApp.xcodeproj
```

## License

MIT
