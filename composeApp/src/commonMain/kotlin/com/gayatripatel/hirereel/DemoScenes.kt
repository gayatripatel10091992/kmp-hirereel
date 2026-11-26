package com.gayatripatel.hirereel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gayatripatel.hirereel.shared.InterviewState
import com.gayatripatel.hirereel.shared.JobFilter
import com.gayatripatel.hirereel.shared.JobType
import com.gayatripatel.hirereel.shared.SampleJobs
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

/**
 * The screens shown in the README, with fixed demo data and a fixed clock, so every platform shows the same moment.
 * Desktop renders them in a JVM test, Android on an emulator and iOS on a simulator: the app opens a scene when it is
 * launched with one (`--es scene 03_jobs` on Android, `-scene 03_jobs` on iOS). Nothing here touches the network.
 */
object DemoScenes {
    class Scene(val name: String, val tab: Tab? = null, val dark: Boolean = false, val content: @Composable () -> Unit)

    private val now = SampleJobs.FIXED_NOW
    private val jobs = SampleJobs.jobs(now)
    private val job = jobs.first()

    val all: List<Scene> = listOf(
        Scene("01_login") {
            LoginScreen(LoginUiState(phone = "4155550134", codeSent = true, code = "3812"), {}, {}, {}, {})
        },
        Scene("02_feed", Tab.FEED) {
            FeedScreen(FeedUiState(jobs, setOf("j1")), rememberPagerState(pageCount = { jobs.size }), {}, {}, video = {}, nowMillis = now)
        },
        Scene("03_jobs", Tab.JOBS) {
            JobsScreen(JobsUiState(JobFilter(), JobFilter().apply(jobs), jobs.size), {}, {}, nowMillis = now)
        },
        Scene("04_jobs_filtered_dark", Tab.JOBS, dark = true) {
            val filter = JobFilter(remoteOnly = true, types = setOf(JobType.FULL_TIME))
            JobsScreen(JobsUiState(filter, filter.apply(jobs), jobs.size), {}, {}, nowMillis = now)
        },
        Scene("05_job_detail") { JobDetailScreen(job, {}, {}, nowMillis = now) },
        interview("06_interview_ready", InterviewState.Ready(0)),
        interview("07_interview_recording", InterviewState.Recording(1, elapsedSeconds = 17, maxSeconds = 45, takesUsed = 1)),
        interview("08_interview_review", InterviewState.Reviewing(2, Take(3, "q3.mp4", 71, takeNumber = 2), retakesLeft = 1)),
        interview(
            "09_interview_finished",
            InterviewState.Finished(2, listOf(Take(1, "a", 48, 1), Take(2, "b", 39, 2), Take(3, "c", 71, 2))),
        ),
        Scene("10_applications", Tab.APPLICATIONS) {
            ApplicationsScreen(ApplicationsUiState(applicationRows(SampleJobs.applications(now), jobs, now), wifiOnly = true)) {}
        },
    )

    fun named(name: String?): Scene? = all.firstOrNull { it.name == name }

    private fun interview(name: String, state: InterviewState) = Scene(name) {
        InterviewScreen(job, state, "Question ${state.questionIndex + 1} of ${job.questions.size}", null, camera = {}, {}, {}, {}, {}, {}, {})
    }
}

/** Draws one demo scene full screen, inside the same theme and tab shell the app uses. */
@Composable
fun DemoScene(scene: DemoScenes.Scene) {
    HireReelTheme(darkTheme = scene.dark) {
        if (scene.tab != null) {
            TabShell(scene.tab, {}) { modifier -> Box(modifier.fillMaxSize()) { scene.content() } }
        } else {
            scene.content()
        }
    }
}
