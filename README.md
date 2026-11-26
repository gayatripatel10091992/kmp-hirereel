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

The shared Compose UI is rendered headlessly on the JVM (Skia) at phone size, and CI commits the images on every push.
Video and camera surfaces are platform views, so the poster frames show instead.

| Sign in | Video feed | Search | Filters (dark) | Job detail |
|---|---|---|---|---|
| ![Login](docs/screenshots/01_login.png) | ![Feed](docs/screenshots/02_feed.png) | ![Jobs](docs/screenshots/03_jobs.png) | ![Filtered](docs/screenshots/04_jobs_filtered_dark.png) | ![Detail](docs/screenshots/05_job_detail.png) |

| Interview: ready | Recording | Review & retake | All answers | Uploads |
|---|---|---|---|---|
| ![Ready](docs/screenshots/06_interview_ready.png) | ![Recording](docs/screenshots/07_interview_recording.png) | ![Review](docs/screenshots/08_interview_review.png) | ![Finished](docs/screenshots/09_interview_finished.png) | ![Applications](docs/screenshots/10_applications.png) |

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
| iOS | `MainViewController()` for the Xcode host app | Poster frames and a simulated recorder (AVPlayer and AVFoundation are the native drop-ins) |
| Desktop | `main.kt` (`./gradlew :composeApp:run`) | Poster frames and a simulated recorder |

## Tests

- **`:shared` commonTest** (runs on the JVM, Android unit tests and the iOS simulator): pool policy, interview flow and
  retake limits, chunking and backoff, filters, formatting, OTP parsing, and `UploadQueue` with virtual time (success,
  retry on 503, stop on 413).
- **Screenshots**: `ScreenshotTest` renders every screen with `ImageComposeScene`.
- **CI**: shared tests plus the Android APK, desktop distributable and screenshots on Ubuntu. `ios.yml` runs the shared
  tests on the iOS simulator and links the iOS framework on macOS.

## Build

```bash
./gradlew :shared:desktopTest           # shared logic on the JVM
./gradlew :composeApp:assembleDebug     # Android APK
./gradlew :composeApp:run               # Desktop app
./gradlew :composeApp:desktopTest       # screenshots → composeApp/build/screenshots
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # iOS framework (macOS)
```

## License

MIT
