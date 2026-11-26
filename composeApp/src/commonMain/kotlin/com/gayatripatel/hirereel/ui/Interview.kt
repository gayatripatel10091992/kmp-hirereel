package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gayatripatel.hirereel.shared.InterviewQuestion
import com.gayatripatel.hirereel.shared.Job
import com.gayatripatel.hirereel.shared.JobFormat
import com.gayatripatel.hirereel.shared.InterviewEvent
import com.gayatripatel.hirereel.shared.InterviewSession
import com.gayatripatel.hirereel.shared.InterviewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun InterviewScreen(
    job: Job,
    state: InterviewState,
    progressLabel: String,
    error: String?,
    camera: @Composable () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetake: () -> Unit,
    onAccept: () -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    val question: InterviewQuestion? = job.questions.getOrNull(state.questionIndex)
    Box(Modifier.fillMaxSize().background(Color(0xFF101014))) {
        if (state !is InterviewState.Finished) {
            Box(Modifier.fillMaxSize()) {
                CameraPlaceholder()
                camera()
            }
        }
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Leave interview", tint = Color.White) }
                Text(
                    if (state is InterviewState.Finished) job.title else progressLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (state is InterviewState.Recording) RecBadge(state)
            }
            if (state is InterviewState.Recording) {
                LinearProgressIndicator(
                    progress = { state.elapsedSeconds / state.maxSeconds.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = RecordRed,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (state is InterviewState.Countdown) {
                    Text("${state.secondsLeft}", color = Color.White, fontSize = 120.sp, fontWeight = FontWeight.Bold)
                }
                if (state is InterviewState.Reviewing) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = "Play your answer", tint = Color.White, modifier = Modifier.size(80.dp))
                }
            }
            error?.let { Text(it, color = RecordRed, modifier = Modifier.padding(16.dp)) }
            BottomPanel(state, question, onStart, onStop, onRetake, onAccept, onSubmit)
        }
    }
}

@Composable
private fun CameraPlaceholder() {
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2B2A33), Color(0xFF15141A)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.12f), modifier = Modifier.size(260.dp))
    }
}

@Composable
private fun RecBadge(state: InterviewState.Recording) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).background(RecordRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            "${JobFormat.timer(state.elapsedSeconds)} / ${JobFormat.timer(state.maxSeconds)}",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun BottomPanel(
    state: InterviewState,
    question: InterviewQuestion?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetake: () -> Unit,
    onAccept: () -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                is InterviewState.Finished -> {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("All ${state.takes.size} answers recorded", style = MaterialTheme.typography.titleLarge)
                    state.takes.forEachIndexed { i, take ->
                        Text(
                            "Answer ${i + 1} · ${JobFormat.timer(take.durationSeconds)}" + if (take.takeNumber > 1) " · take ${take.takeNumber}" else "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Submit application") }
                    Text(
                        "Videos upload in the background, even if you close the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                else -> {
                    Text(
                        question?.prompt.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    question?.let {
                        Text(
                            "Up to ${it.maxSeconds}s · ${it.retakesAllowed} retakes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    when (state) {
                        is InterviewState.Ready -> Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Icon(Icons.Filled.Videocam, null)
                            Spacer(Modifier.width(8.dp))
                            Text("I'm ready")
                        }
                        is InterviewState.Countdown -> Text("Recording starts in ${state.secondsLeft}…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        is InterviewState.Recording -> Box(
                            Modifier.size(72.dp).border(4.dp, RecordRed, CircleShape).padding(14.dp)
                                .clip(RoundedCornerShape(8.dp)).background(RecordRed).clickable(onClick = onStop),
                        )
                        is InterviewState.Reviewing -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = onRetake, enabled = state.retakesLeft > 0, modifier = Modifier.weight(1f).height(52.dp)) {
                                Icon(Icons.Filled.Replay, null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (state.retakesLeft > 0) "Retake (${state.retakesLeft})" else "No retakes left")
                            }
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            ) { Text("Use this answer") }
                        }
                        is InterviewState.Finished -> Unit
                    }
                }
            }
        }
    }
}

