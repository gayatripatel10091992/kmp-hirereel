package com.gayatripatel.hirereel.shared

/** One recorded answer the candidate chose to keep. */
data class Take(val questionId: Int, val filePath: String, val durationSeconds: Int, val takeNumber: Int)

sealed interface InterviewState {
    val questionIndex: Int

    data class Ready(override val questionIndex: Int, val takesUsed: Int = 0) : InterviewState
    data class Countdown(override val questionIndex: Int, val secondsLeft: Int, val takesUsed: Int) : InterviewState
    data class Recording(override val questionIndex: Int, val elapsedSeconds: Int, val maxSeconds: Int, val takesUsed: Int) : InterviewState {
        val secondsLeft: Int get() = maxSeconds - elapsedSeconds
    }
    data class Reviewing(override val questionIndex: Int, val take: Take, val retakesLeft: Int) : InterviewState
    data class Finished(override val questionIndex: Int, val takes: List<Take>) : InterviewState
}

sealed interface InterviewEvent {
    data object Start : InterviewEvent
    data object Tick : InterviewEvent
    data class Saved(val filePath: String) : InterviewEvent
    data object Retake : InterviewEvent
    data object Accept : InterviewEvent
}

/**
 * One-way video interview: prep countdown, timed answer that auto-stops at the limit,
 * then review with a limited number of retakes. Pure, so every path is unit-tested.
 */
class InterviewSession(private val questions: List<InterviewQuestion>) {

    private val kept = mutableListOf<Take>()
    var state: InterviewState = if (questions.isEmpty()) InterviewState.Finished(0, emptyList()) else InterviewState.Ready(0)
        private set

    /** The UI layer starts CameraX recording while this is true and stops it when it turns false. */
    val shouldRecord: Boolean get() = state is InterviewState.Recording

    val question: InterviewQuestion? get() = questions.getOrNull(state.questionIndex)
    val progressLabel: String get() = "Question ${(state.questionIndex + 1).coerceAtMost(questions.size)} of ${questions.size}"

    fun on(event: InterviewEvent): InterviewState {
        state = next(state, event)
        return state
    }

    private fun next(s: InterviewState, e: InterviewEvent): InterviewState = when (s) {
        is InterviewState.Ready -> if (e == InterviewEvent.Start) {
            InterviewState.Countdown(s.questionIndex, questions[s.questionIndex].prepSeconds, s.takesUsed)
        } else s

        is InterviewState.Countdown -> when (e) {
            InterviewEvent.Tick ->
                if (s.secondsLeft > 1) s.copy(secondsLeft = s.secondsLeft - 1)
                else InterviewState.Recording(s.questionIndex, 0, questions[s.questionIndex].maxSeconds, s.takesUsed + 1)
            else -> s
        }

        is InterviewState.Recording -> when (e) {
            // Stopping is the recorder's job (user tap or time limit); the take arrives with the Saved event.
            InterviewEvent.Tick -> s.copy(elapsedSeconds = (s.elapsedSeconds + 1).coerceAtMost(s.maxSeconds))
            is InterviewEvent.Saved -> {
                val q = questions[s.questionIndex]
                InterviewState.Reviewing(
                    s.questionIndex,
                    Take(q.id, e.filePath, s.elapsedSeconds.coerceAtLeast(1), s.takesUsed),
                    retakesLeft = (q.retakesAllowed + 1 - s.takesUsed).coerceAtLeast(0),
                )
            }
            else -> s
        }

        is InterviewState.Reviewing -> when (e) {
            InterviewEvent.Retake ->
                if (s.retakesLeft > 0) InterviewState.Countdown(s.questionIndex, questions[s.questionIndex].prepSeconds, s.take.takeNumber)
                else s
            InterviewEvent.Accept -> {
                kept += s.take
                val nextIndex = s.questionIndex + 1
                if (nextIndex < questions.size) InterviewState.Ready(nextIndex) else InterviewState.Finished(s.questionIndex, kept.toList())
            }
            else -> s
        }

        is InterviewState.Finished -> s
    }

    /** True when the recorder should be stopped because the answer reached its time limit. */
    fun reachedLimit(): Boolean = (state as? InterviewState.Recording)?.let { it.elapsedSeconds >= it.maxSeconds } == true
}
