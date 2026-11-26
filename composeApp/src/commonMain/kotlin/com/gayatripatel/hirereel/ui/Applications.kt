package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gayatripatel.hirereel.shared.ApplicationStatus
import com.gayatripatel.hirereel.shared.Job
import com.gayatripatel.hirereel.shared.JobApplication
import com.gayatripatel.hirereel.shared.JobFormat
import com.gayatripatel.hirereel.shared.progressLabel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ApplicationRowUi(
    val id: String,
    val jobTitle: String,
    val company: String,
    val status: ApplicationStatus,
    val answers: Int,
    val progress: Float?,
    val detail: String,
)

data class ApplicationsUiState(val rows: List<ApplicationRowUi> = emptyList(), val wifiOnly: Boolean = true)

fun applicationRows(apps: List<JobApplication>, jobs: List<Job>, nowMillis: Long): List<ApplicationRowUi> {
    val byId = jobs.associateBy { it.id }
    return apps.sortedByDescending { it.updatedAtMillis }.mapNotNull { app ->
        val job = byId[app.jobId] ?: return@mapNotNull null
        val uploading = app.status == ApplicationStatus.UPLOADING || app.status == ApplicationStatus.WAITING_FOR_NETWORK
        ApplicationRowUi(
            id = app.id,
            jobTitle = job.title,
            company = job.company,
            status = app.status,
            answers = app.answers,
            progress = if (uploading) app.progress else null,
            detail = if (uploading) progressLabel(app.uploadedBytes, app.totalBytes)
            else "${app.answers} video answers · updated ${JobFormat.posted(app.updatedAtMillis, nowMillis).replace("Just posted", "just now")}",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(state: ApplicationsUiState, onWifiOnlyChange: (Boolean) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("My applications") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Wifi, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Upload videos on Wi-Fi only", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Interview videos are 10–50 MB. Uploads pause on mobile data and resume where they stopped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = state.wifiOnly, onCheckedChange = onWifiOnlyChange)
                    }
                }
            }
            items(state.rows, key = { it.id }) { row ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CompanyLogo(row.company, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(row.jobTitle, style = MaterialTheme.typography.titleMedium)
                                Text(row.company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(row.status)
                        }
                        Spacer(Modifier.height(12.dp))
                        row.progress?.let { p ->
                            LinearProgressIndicator(
                                progress = { p },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (row.status == ApplicationStatus.WAITING_FOR_NETWORK) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(row.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
