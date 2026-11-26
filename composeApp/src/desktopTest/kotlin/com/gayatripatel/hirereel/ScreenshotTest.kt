package com.gayatripatel.hirereel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.gayatripatel.hirereel.shared.JobFilter
import com.gayatripatel.hirereel.shared.JobType
import com.gayatripatel.hirereel.shared.SampleJobs
import com.gayatripatel.hirereel.shared.InterviewState
import com.gayatripatel.hirereel.shared.Take
import com.gayatripatel.hirereel.ui.ApplicationsScreen
import com.gayatripatel.hirereel.ui.ApplicationsUiState
import com.gayatripatel.hirereel.ui.FeedScreen
import com.gayatripatel.hirereel.ui.FeedUiState
import com.gayatripatel.hirereel.ui.HireReelTheme
import com.gayatripatel.hirereel.ui.InterviewScreen
import com.gayatripatel.hirereel.ui.JobDetailScreen
import com.gayatripatel.hirereel.ui.JobsScreen
import com.gayatripatel.hirereel.ui.JobsUiState
import com.gayatripatel.hirereel.ui.LoginScreen
import com.gayatripatel.hirereel.ui.LoginUiState
import com.gayatripatel.hirereel.ui.applicationRows
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renders the shared Compose Multiplatform screens headlessly on the JVM (Skia) at phone size.
 * These are the same composables the Android, iOS and Desktop apps run. CI copies the PNGs to docs/screenshots.
 */
@OptIn(ExperimentalComposeUiApi::class)
class ScreenshotTest {
    private val now = SampleJobs.FIXED_NOW
    private val jobs = SampleJobs.jobs(now)
    private val job = jobs.first()

    @Test fun login() = capture("01_login") {
        LoginScreen(LoginUiState(phone = "4155550134", codeSent = true, code = "3812"), {}, {}, {}, {})
    }

    @Test fun feed() = capture("02_feed", Tab.FEED) {
        FeedScreen(FeedUiState(jobs, setOf("j1")), rememberPagerState(pageCount = { jobs.size }), {}, {}, video = {}, nowMillis = now)
    }

    @Test fun jobs() = capture("03_jobs", Tab.JOBS) {
        JobsScreen(JobsUiState(JobFilter(), JobFilter().apply(jobs), jobs.size), {}, {}, nowMillis = now)
    }

    @Test fun jobsFilteredDark() = capture("04_jobs_filtered_dark", Tab.JOBS, dark = true) {
        val filter = JobFilter(remoteOnly = true, types = setOf(JobType.FULL_TIME))
        JobsScreen(JobsUiState(filter, filter.apply(jobs), jobs.size), {}, {}, nowMillis = now)
    }

    @Test fun jobDetail() = capture("05_job_detail") { JobDetailScreen(job, {}, {}, nowMillis = now) }

    @Test fun interviewReady() = interview("06_interview_ready", InterviewState.Ready(0))

    @Test fun interviewRecording() = interview(
        "07_interview_recording",
        InterviewState.Recording(1, elapsedSeconds = 17, maxSeconds = 45, takesUsed = 1),
    )

    @Test fun interviewReview() = interview(
        "08_interview_review",
        InterviewState.Reviewing(2, Take(3, "q3.mp4", 71, takeNumber = 2), retakesLeft = 1),
    )

    @Test fun interviewFinished() = interview(
        "09_interview_finished",
        InterviewState.Finished(2, listOf(Take(1, "a", 48, 1), Take(2, "b", 39, 2), Take(3, "c", 71, 2))),
    )

    @Test fun applications() = capture("10_applications", Tab.APPLICATIONS) {
        ApplicationsScreen(ApplicationsUiState(applicationRows(SampleJobs.applications(now), jobs, now), wifiOnly = true)) {}
    }

    private fun interview(name: String, state: InterviewState) = capture(name) {
        InterviewScreen(job, state, "Question ${state.questionIndex + 1} of ${job.questions.size}", null, camera = {}, {}, {}, {}, {}, {}, {})
    }

    private fun capture(name: String, tab: Tab? = null, dark: Boolean = false, content: @Composable () -> Unit) {
        val density = 2.625f
        val file = File("build/screenshots/$name.png")
        val scene = ImageComposeScene(
            width = (411 * density).toInt(),
            height = (891 * density).toInt(),
            density = Density(density),
        ) {
            HireReelTheme(darkTheme = dark) {
                if (tab != null) TabShell(tab, {}) { m -> Box(m) { content() } } else content()
            }
        }
        try {
            scene.render()
            val image = scene.render(nanoTime = 500_000_000L)
            val png = image.encodeToData(EncodedImageFormat.PNG) ?: error("PNG encoding failed")
            file.parentFile.mkdirs()
            file.writeBytes(png.bytes)
        } finally {
            scene.close()
        }
        assertTrue(file.length() > 0, "Screenshot $name was not written")
    }
}
