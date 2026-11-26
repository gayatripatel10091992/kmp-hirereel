package com.gayatripatel.hirereel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.gayatripatel.hirereel.shared.InterviewEvent
import com.gayatripatel.hirereel.shared.InterviewState
import com.gayatripatel.hirereel.shared.PlayerPoolPolicy
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

/** Bottom-navigation shell, shared by the app and the screenshot tests. */
@Composable
fun TabShell(selected: Tab?, onSelect: (Tab) -> Unit, content: @Composable (Modifier) -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (selected != null) {
                NavigationBar {
                    listOf(
                        Triple(Tab.FEED, "For you", Icons.Filled.PlayCircle),
                        Triple(Tab.JOBS, "Search", Icons.Filled.Search),
                        Triple(Tab.APPLICATIONS, "Applied", Icons.AutoMirrored.Filled.Assignment),
                    ).forEach { (tab, label, icon) ->
                        NavigationBarItem(
                            selected = tab == selected,
                            onClick = { onSelect(tab) },
                            icon = { Icon(icon, null) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding -> content(Modifier.padding(padding)) }
}

@Composable
fun App(viewModel: HireReelViewModel) {
    HireReelTheme {
        val backStack by viewModel.backStack.collectAsState()
        when (val screen = backStack.last()) {
            Screen.Login -> {
                val login by viewModel.login.collectAsState()
                LoginScreen(
                    LoginUiState(login.phone, login.codeSent, login.code, login.error),
                    viewModel::onPhone, viewModel::sendCode, viewModel::onCode, viewModel::verify,
                )
            }
            is Screen.Home -> TabShell(screen.tab, viewModel::selectTab) { modifier ->
                Box(modifier.fillMaxSize()) { HomeTab(viewModel, screen.tab) }
            }
            is Screen.JobDetail -> viewModel.job(screen.jobId)?.let { job ->
                JobDetailScreen(job, viewModel::back, { viewModel.startInterview(job.id) }, viewModel.clock.now())
            }
            is Screen.Interview -> InterviewRoute(viewModel, screen.jobId)
        }
    }
}

@Composable
private fun HomeTab(viewModel: HireReelViewModel, tab: Tab) {
    val jobs by viewModel.jobs.jobs.collectAsState()
    when (tab) {
        Tab.FEED -> {
            val saved by viewModel.jobs.saved.collectAsState()
            val pager = rememberPagerState(pageCount = { jobs.size })
            // Shared pooling policy: only the settled page and its neighbours may hold a player.
            val plan = PlayerPoolPolicy.plan(pager.settledPage, jobs.size, emptySet(), pager.isScrollInProgress)
            FeedScreen(
                state = FeedUiState(jobs, saved),
                pagerState = pager,
                onApply = { viewModel.open(Screen.JobDetail(it)) },
                onToggleSaved = viewModel::toggleSaved,
                video = { page ->
                    if (page in plan.prepare) FeedVideo(jobs[page].videoUrl, page == plan.play, Modifier.fillMaxSize())
                },
                nowMillis = viewModel.clock.now(),
            )
        }
        Tab.JOBS -> {
            val filter by viewModel.filter.collectAsState()
            JobsScreen(
                JobsUiState(filter, filter.apply(jobs), jobs.size),
                viewModel::updateFilter,
                { viewModel.open(Screen.JobDetail(it)) },
                viewModel.clock.now(),
            )
        }
        Tab.APPLICATIONS -> {
            val apps by viewModel.applications.applications.collectAsState()
            ApplicationsScreen(ApplicationsUiState(applicationRows(apps, jobs, viewModel.clock.now()), wifiOnly = true)) {}
        }
    }
}

@Composable
private fun InterviewRoute(viewModel: HireReelViewModel, jobId: String) {
    val job = viewModel.job(jobId) ?: return
    val state by viewModel.interview.collectAsState()
    val current = state ?: return
    val recorder = rememberVideoRecorder()
    val recording = current is InterviewState.Recording

    LaunchedEffect(recording) {
        if (recording) {
            val take = (current as InterviewState.Recording).takesUsed
            recorder.start(
                "${job.id}-q${current.questionIndex + 1}-take$take.mp4",
                onSaved = { path, size -> viewModel.onRecorded(path, size) },
                onError = {},
            )
        }
    }
    LaunchedEffect(current) { if (viewModel.reachedLimit) recorder.stop() }

    InterviewScreen(
        job = job,
        state = current,
        progressLabel = viewModel.progressLabel(),
        error = null,
        camera = { CameraPreview(recorder, Modifier.fillMaxSize()) },
        onStart = { viewModel.send(InterviewEvent.Start) },
        onStop = { recorder.stop() },
        onRetake = { viewModel.send(InterviewEvent.Retake) },
        onAccept = { viewModel.send(InterviewEvent.Accept) },
        onSubmit = { viewModel.submit(jobId) },
        onClose = viewModel::leaveInterview,
    )
}
