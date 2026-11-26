package com.gayatripatel.hirereel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gayatripatel.hirereel.shared.ApplicationStatus
import com.gayatripatel.hirereel.shared.Job

/** Gradient poster shown before a job video's first frame (and in screenshots, where video can't render). */
fun Job.posterBrush(): Brush = Brush.linearGradient(listOf(Color(posterColors.first), Color(posterColors.second)))

@Composable
fun CompanyLogo(company: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(size / 4)).background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            company.split(' ').take(2).joinToString("") { it.first().uppercase() },
            color = Color(0xFF1B1A21),
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp,
        )
    }
}

@Composable
fun StatusPill(status: ApplicationStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (status) {
        ApplicationStatus.SHORTLISTED -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        ApplicationStatus.NOT_SELECTED -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
        ApplicationStatus.WAITING_FOR_NETWORK -> scheme.secondaryContainer to scheme.onSecondaryContainer
        else -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
    Text(
        status.label,
        modifier = modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelMedium,
    )
}
