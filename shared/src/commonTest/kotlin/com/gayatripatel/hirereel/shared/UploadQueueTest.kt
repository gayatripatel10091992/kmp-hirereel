package com.gayatripatel.hirereel.shared

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UploadQueueTest {
    private val files = listOf(UploadFile("q1.mp4", 2_500_000), UploadFile("q2.mp4", 1_000_000))

    @Test
    fun uploadsAllChunksAndSubmits() = runTest {
        val repo = ApplicationRepository { 0L }
        val app = repo.create("j1", answers = 2, totalBytes = 3_500_000)
        val queue = UploadQueue(this, FakeUploadApi(chunkDelayMillis = 10), repo, chunkBytes = 1_000_000)
        queue.enqueue(app.id, files)
        advanceUntilIdle()
        val done = repo.get(app.id)!!
        assertEquals(ApplicationStatus.SUBMITTED, done.status)
        assertEquals(3_500_000L, done.uploadedBytes)
    }

    @Test
    fun retriesServerErrorsAndResumesWhereItStopped() = runTest {
        val repo = ApplicationRepository { 0L }
        val app = repo.create("j1", answers = 2, totalBytes = 3_500_000)
        val api = FakeUploadApi(chunkDelayMillis = 10).apply { failures.addAll(listOf(503, 503)) }
        UploadQueue(this, api, repo, chunkBytes = 1_000_000).enqueue(app.id, files)
        advanceUntilIdle()
        assertEquals(ApplicationStatus.SUBMITTED, repo.get(app.id)!!.status)
    }

    @Test
    fun clientErrorsStopWithoutRetrying() {
        val scope = TestScope(StandardTestDispatcher())
        val repo = ApplicationRepository { 0L }
        val app = repo.create("j1", answers = 2, totalBytes = 3_500_000)
        val api = FakeUploadApi(chunkDelayMillis = 10).apply { failures.add(413) }
        UploadQueue(scope, api, repo, chunkBytes = 1_000_000).enqueue(app.id, files)
        scope.advanceUntilIdle()
        assertEquals(ApplicationStatus.WAITING_FOR_NETWORK, repo.get(app.id)!!.status)
        assertEquals(0L, repo.get(app.id)!!.uploadedBytes)
    }
}
