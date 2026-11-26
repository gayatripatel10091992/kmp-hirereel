package com.gayatripatel.hirereel.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

fun interface Clock {
    fun now(): Long
}

/** Jobs and saved jobs. Shared by Android, iOS and Desktop. */
class JobRepository(clock: Clock) {
    private val _jobs = MutableStateFlow(SampleJobs.jobs(clock.now()))
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _saved = MutableStateFlow(setOf("j4"))
    val saved: StateFlow<Set<String>> = _saved.asStateFlow()

    fun job(id: String): Job? = _jobs.value.firstOrNull { it.id == id }

    fun toggleSaved(id: String) = _saved.update { if (id in it) it - id else it + id }
}

class ApplicationRepository(private val clock: Clock) {
    private val _applications = MutableStateFlow(SampleJobs.applications(clock.now()))
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    fun create(jobId: String, answers: Int, totalBytes: Long): JobApplication {
        val app = JobApplication("a${clock.now()}", jobId, ApplicationStatus.UPLOADING, answers, totalBytes, 0, clock.now())
        _applications.update { listOf(app) + it }
        return app
    }

    fun setStatus(id: String, status: ApplicationStatus) = modify(id) { it.copy(status = status, updatedAtMillis = clock.now()) }

    fun setProgress(id: String, uploaded: Long, total: Long) =
        modify(id) { it.copy(uploadedBytes = uploaded, totalBytes = total, status = ApplicationStatus.UPLOADING) }

    fun get(id: String): JobApplication? = _applications.value.firstOrNull { it.id == id }

    private fun modify(id: String, transform: (JobApplication) -> JobApplication) =
        _applications.update { list -> list.map { if (it.id == id) transform(it) else it } }
}
