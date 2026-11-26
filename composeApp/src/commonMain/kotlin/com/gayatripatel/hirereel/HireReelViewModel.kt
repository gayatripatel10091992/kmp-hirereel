package com.gayatripatel.hirereel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gayatripatel.hirereel.shared.ApplicationRepository
import com.gayatripatel.hirereel.shared.Clock
import com.gayatripatel.hirereel.shared.FakeUploadApi
import com.gayatripatel.hirereel.shared.InterviewEvent
import com.gayatripatel.hirereel.shared.InterviewSession
import com.gayatripatel.hirereel.shared.InterviewState
import com.gayatripatel.hirereel.shared.Job
import com.gayatripatel.hirereel.shared.JobFilter
import com.gayatripatel.hirereel.shared.JobRepository
import com.gayatripatel.hirereel.shared.OtpParser
import com.gayatripatel.hirereel.shared.UploadFile
import com.gayatripatel.hirereel.shared.UploadQueue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Tab { FEED, JOBS, APPLICATIONS }

sealed interface Screen {
    data object Login : Screen
    data class Home(val tab: Tab) : Screen
    data class JobDetail(val jobId: String) : Screen
    data class Interview(val jobId: String) : Screen
}

data class LoginState(val phone: String = "", val codeSent: Boolean = false, val code: String = "", val error: String? = null)

/**
 * The app's shared view model (JetBrains multiplatform lifecycle): navigation, sign-in, job filters and the
 * interview session. It survives configuration changes on Android and is a plain object on Desktop and iOS.
 */
class HireReelViewModel(
    val clock: Clock = Clock { kotlinx.datetime.Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    val jobs = JobRepository(clock)
    val applications = ApplicationRepository(clock)
    private val uploads = UploadQueue(viewModelScope, FakeUploadApi(), applications)

    private val _backStack = MutableStateFlow<List<Screen>>(listOf(Screen.Login))
    val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()

    private val _login = MutableStateFlow(LoginState())
    val login: StateFlow<LoginState> = _login.asStateFlow()

    private val _filter = MutableStateFlow(JobFilter())
    val filter: StateFlow<JobFilter> = _filter.asStateFlow()

    private var session: InterviewSession? = null
    private val _interview = MutableStateFlow<InterviewState?>(null)
    val interview: StateFlow<InterviewState?> = _interview.asStateFlow()
    private var ticker: kotlinx.coroutines.Job? = null
    private val sizes = mutableMapOf<String, Long>()

    // Navigation
    fun open(screen: Screen) = _backStack.update { it + screen }

    fun back() = _backStack.update { if (it.size > 1) it.dropLast(1) else it }

    fun selectTab(tab: Tab) = _backStack.update { listOf(Screen.Home(tab)) }

    // Sign-in with a one-time code
    fun onPhone(value: String) = _login.update { it.copy(phone = value.filter(Char::isDigit).take(10), error = null) }

    fun sendCode() = _login.update {
        if (it.phone.length == 10) it.copy(codeSent = true, error = null) else it.copy(error = "Enter a 10-digit mobile number")
    }

    fun onCode(value: String) = _login.update { it.copy(code = value.filter(Char::isDigit).take(6), error = null) }

    /** Android passes SMS Retriever messages here; the shared parser extracts the code. */
    fun onSms(message: String) {
        OtpParser.parse(message)?.let {
            onCode(it)
            verify()
        }
    }

    fun verify() {
        if (_login.value.code.length != 6) {
            _login.update { it.copy(error = "Enter the 6-digit code") }
            return
        }
        _backStack.value = listOf(Screen.Home(Tab.FEED)) // demo backend accepts any code
    }

    fun updateFilter(transform: (JobFilter) -> JobFilter) = _filter.update(transform)

    fun toggleSaved(id: String) = jobs.toggleSaved(id)

    // Interview
    fun job(id: String): Job? = jobs.job(id)

    fun startInterview(jobId: String) {
        val job = jobs.job(jobId) ?: return
        session = InterviewSession(job.questions)
        sizes.clear()
        _interview.value = session?.state
        open(Screen.Interview(jobId))
    }

    fun send(event: InterviewEvent) {
        val s = session ?: return
        _interview.value = s.on(event)
        val state = _interview.value
        val needsClock = state is InterviewState.Countdown || state is InterviewState.Recording
        if (needsClock && ticker?.isActive != true) {
            ticker = viewModelScope.launch {
                while (isActive) {
                    delay(1_000)
                    send(InterviewEvent.Tick)
                }
            }
        } else if (!needsClock) {
            ticker?.cancel()
        }
    }

    fun onRecorded(path: String, sizeBytes: Long) {
        sizes[path] = sizeBytes
        send(InterviewEvent.Saved(path))
    }

    val reachedLimit: Boolean get() = session?.reachedLimit() == true

    fun progressLabel(): String = session?.progressLabel.orEmpty()

    fun leaveInterview() {
        ticker?.cancel()
        session = null
        _interview.value = null
        back()
    }

    /** Creates the application and starts the resumable chunked upload of every kept answer. */
    fun submit(jobId: String) {
        val finished = _interview.value as? InterviewState.Finished ?: return
        val files = finished.takes.map { UploadFile(it.filePath.substringAfterLast('/'), sizes[it.filePath] ?: 20_000_000L) }
        val app = applications.create(jobId, finished.takes.size, files.sumOf { it.sizeBytes })
        uploads.enqueue(app.id, files)
        session = null
        _interview.value = null
        selectTab(Tab.APPLICATIONS)
    }
}
