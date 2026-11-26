package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gayatripatel.hirereel.shared.Job
import com.gayatripatel.hirereel.shared.JobFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FeedUiState(val jobs: List<Job> = emptyList(), val saved: Set<String> = emptySet())

@Composable
fun FeedScreen(
    state: FeedUiState,
    pagerState: PagerState,
    onApply: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
    video: @Composable (page: Int) -> Unit,
    nowMillis: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize(), key = { state.jobs[it].id }) { page ->
            val job = state.jobs[page]
            FeedPage(job, job.id in state.saved, nowMillis, { onApply(job.id) }, { onToggleSaved(job.id) }) { video(page) }
        }
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("For you", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.VolumeOff, contentDescription = "Muted", tint = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun FeedPage(
    job: Job,
    saved: Boolean,
    nowMillis: Long,
    onApply: () -> Unit,
    onToggleSaved: () -> Unit,
    video: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(job.posterBrush())) {
        video()
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)),
            ),
        )
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RoundAction(if (saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, if (saved) "Saved" else "Save", onToggleSaved)
            RoundAction(Icons.Filled.Share, "Share") {}
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompanyLogo(job.company, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(job.company, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(JobFormat.posted(job.postedAtMillis, nowMillis), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(job.title, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "${JobFormat.location(job)} · ${job.type.label}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Tag(JobFormat.salary(job), strong = true)
                job.tags.take(2).forEach { Tag(it) }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Icon(Icons.Filled.Videocam, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Apply with a video", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                JobFormat.applicants(job.applicants),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            )
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun Tag(text: String, strong: Boolean = false) {
    Text(
        text,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (strong) Color.White else Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = if (strong) Color(0xFF1B1A21) else Color.White,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.3f))) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
