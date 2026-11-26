package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gayatripatel.hirereel.shared.Job
import com.gayatripatel.hirereel.shared.JobFilter
import com.gayatripatel.hirereel.shared.JobFormat
import com.gayatripatel.hirereel.shared.JobType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class JobsUiState(val filter: JobFilter = JobFilter(), val results: List<Job> = emptyList(), val total: Int = 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    state: JobsUiState,
    onFilter: ((JobFilter) -> JobFilter) -> Unit,
    onOpen: (String) -> Unit,
    nowMillis: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
) {
    val f = state.filter
    Scaffold(topBar = { TopAppBar(title = { Text("Find jobs") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = f.query,
                    onValueChange = { q -> onFilter { it.copy(query = q) } },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    placeholder = { Text("Title, company, skill or city") },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(f.remoteOnly, { onFilter { it.copy(remoteOnly = !it.remoteOnly) } }, label = { Text("Remote") })
                    FilterChip(f.minSalaryK >= 100, { onFilter { it.copy(minSalaryK = if (it.minSalaryK >= 100) 0 else 100) } }, label = { Text("\$100k+") })
                    JobType.entries.forEach { type ->
                        FilterChip(
                            type in f.types,
                            { onFilter { it.copy(types = if (type in it.types) it.types - type else it.types + type) } },
                            label = { Text(type.label) },
                        )
                    }
                }
            }
            item {
                Text(
                    if (f.isActive) "${state.results.size} of ${state.total} jobs" else "${state.total} jobs near you and remote",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.results, key = { it.id }) { job -> JobCard(job, nowMillis) { onOpen(job.id) } }
        }
    }
}

@Composable
private fun JobCard(job: Job, nowMillis: Long, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp)) {
            Box(
                Modifier.size(width = 84.dp, height = 112.dp).clip(RoundedCornerShape(12.dp)).background(job.posterBrush()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = "Has intro video", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(job.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(job.company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${JobFormat.location(job)} · ${job.type.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(JobFormat.salary(job), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${JobFormat.posted(job.postedAtMillis, nowMillis)} · ${JobFormat.applicants(job.applicants)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(job: Job, onBack: () -> Unit, onStartInterview: () -> Unit, nowMillis: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) {
    val totalSeconds = job.questions.sumOf { it.maxSeconds }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            Button(
                onClick = onStartInterview,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Icon(Icons.Filled.Videocam, null)
                Spacer(Modifier.width(8.dp))
                Text("Start video interview")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(240.dp).background(job.posterBrush()), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PlayCircle, contentDescription = "Play intro video", tint = Color.White, modifier = Modifier.size(64.dp))
            }
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CompanyLogo(job.company, size = 44.dp, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(11.dp)))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(job.company, style = MaterialTheme.typography.titleMedium)
                        Text(JobFormat.posted(job.postedAtMillis, nowMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${JobFormat.location(job)} · ${job.type.label} · ${JobFormat.salary(job)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text(job.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(20.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Video interview · ${job.questions.size} questions · about ${(totalSeconds + 59) / 60} min",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        job.questions.forEachIndexed { i, q ->
                            Text(
                                "${i + 1}. Up to ${q.maxSeconds}s · ${q.retakesAllowed} retakes",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Questions are revealed one at a time. You can review each answer before moving on.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
