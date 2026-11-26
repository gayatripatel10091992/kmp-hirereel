package com.gayatripatel.hirereel.shared

import kotlin.math.min
import kotlin.math.pow

/** Byte range of one chunk of a resumable upload, end exclusive. */
data class Chunk(val index: Int, val start: Long, val endExclusive: Long) {
    val size: Long get() = endExclusive - start
}

/**
 * Splits interview videos into chunks for a resumable upload. If the worker is stopped
 * (network lost, app killed), it resumes from the first byte the server hasn't confirmed instead of starting over.
 */
object ChunkPlanner {
    const val DEFAULT_CHUNK = 1L * 1024 * 1024

    fun plan(totalBytes: Long, chunkBytes: Long = DEFAULT_CHUNK): List<Chunk> {
        require(chunkBytes > 0) { "chunk size must be positive" }
        if (totalBytes <= 0) return emptyList()
        val count = ((totalBytes + chunkBytes - 1) / chunkBytes).toInt()
        return List(count) { i -> Chunk(i, i * chunkBytes, min((i + 1) * chunkBytes, totalBytes)) }
    }

    /** Chunks still to send when the server has confirmed [confirmedBytes]. A partly-sent chunk is re-sent whole. */
    fun remaining(totalBytes: Long, confirmedBytes: Long, chunkBytes: Long = DEFAULT_CHUNK): List<Chunk> =
        plan(totalBytes, chunkBytes).filter { it.endExclusive > confirmedBytes }
}

/** Exponential backoff with a cap, matching what WorkManager does between worker retries. */
object RetryPolicy {
    fun delayMillis(attempt: Int, baseMillis: Long = 10_000, maxMillis: Long = 5 * 60_000): Long {
        if (attempt <= 0) return 0
        val raw = baseMillis * 2.0.pow(attempt - 1)
        return min(raw.toLong(), maxMillis)
    }

    /** Client errors won't succeed on retry; server and network errors might. */
    fun isRetryable(httpStatus: Int?): Boolean = when (httpStatus) {
        null -> true
        408, 429 -> true
        in 500..599 -> true
        else -> false
    }
}

/** "12.4 MB of 48.0 MB" */
fun progressLabel(uploaded: Long, total: Long): String = "${megabytes(uploaded)} of ${megabytes(total)}"

fun megabytes(bytes: Long): String {
    val tenths = (bytes * 10 + 512 * 1024) / (1024 * 1024)
    return "${tenths / 10}.${tenths % 10} MB"
}
