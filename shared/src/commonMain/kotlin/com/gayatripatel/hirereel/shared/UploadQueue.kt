package com.gayatripatel.hirereel.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChunkResult(val ok: Boolean, val httpStatus: Int? = null)

/** One answer video waiting to upload. `name` identifies it on the server for resuming. */
data class UploadFile(val name: String, val sizeBytes: Long)

/** Resumable upload API (a signed-URL multipart upload in production). */
interface UploadApi {
    suspend fun confirmedBytes(applicationId: String, fileName: String): Long
    suspend fun sendChunk(applicationId: String, file: UploadFile, chunk: Chunk): ChunkResult
    suspend fun complete(applicationId: String)
}

class FakeUploadApi(private val chunkDelayMillis: Long = 120) : UploadApi {
    private val confirmed = mutableMapOf<String, Long>()
    /** Statuses returned for the next calls, to simulate failures in tests. */
    val failures = ArrayDeque<Int>()

    override suspend fun confirmedBytes(applicationId: String, fileName: String): Long = confirmed["$applicationId/$fileName"] ?: 0L

    override suspend fun sendChunk(applicationId: String, file: UploadFile, chunk: Chunk): ChunkResult {
        delay(chunkDelayMillis)
        failures.removeFirstOrNull()?.let { return ChunkResult(ok = false, httpStatus = it) }
        confirmed["$applicationId/${file.name}"] = chunk.endExclusive
        return ChunkResult(ok = true, httpStatus = 200)
    }

    override suspend fun complete(applicationId: String) = delay(chunkDelayMillis)
}

/**
 * Platform-neutral upload loop: chunked, resumable from the server's confirmed offset, with capped exponential
 * backoff for retryable errors. The Android app can run it inside WorkManager; iOS and Desktop run it in-process.
 */
class UploadQueue(
    private val scope: CoroutineScope,
    private val api: UploadApi,
    private val applications: ApplicationRepository,
    private val maxAttempts: Int = 6,
    private val chunkBytes: Long = ChunkPlanner.DEFAULT_CHUNK,
) {
    private val running = mutableMapOf<String, Job>()

    fun enqueue(applicationId: String, files: List<UploadFile>): Job {
        running[applicationId]?.let { if (it.isActive) return it }
        val job = scope.launch { upload(applicationId, files) }
        running[applicationId] = job
        return job
    }

    private suspend fun upload(applicationId: String, files: List<UploadFile>) {
        val total = files.sumOf { it.sizeBytes }
        var attempt = 0
        while (true) {
            when (val outcome = tryOnce(applicationId, files, total)) {
                null -> {
                    api.complete(applicationId)
                    applications.setStatus(applicationId, ApplicationStatus.SUBMITTED)
                    return
                }
                else -> {
                    attempt++
                    if (!RetryPolicy.isRetryable(outcome.takeIf { it > 0 }) || attempt >= maxAttempts) {
                        applications.setStatus(applicationId, ApplicationStatus.WAITING_FOR_NETWORK)
                        return
                    }
                    applications.setStatus(applicationId, ApplicationStatus.WAITING_FOR_NETWORK)
                    delay(RetryPolicy.delayMillis(attempt))
                }
            }
        }
    }

    /** Returns null on success, or the failing HTTP status (0 when the request never got a response). */
    private suspend fun tryOnce(applicationId: String, files: List<UploadFile>, total: Long): Int? {
        var doneBefore = 0L
        for (file in files) {
            val confirmed = api.confirmedBytes(applicationId, file.name)
            for (chunk in ChunkPlanner.remaining(file.sizeBytes, confirmed, chunkBytes)) {
                val result = api.sendChunk(applicationId, file, chunk)
                if (!result.ok) return result.httpStatus ?: 0
                applications.setProgress(applicationId, doneBefore + chunk.endExclusive, total)
            }
            doneBefore += file.sizeBytes
        }
        return null
    }
}
