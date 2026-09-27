package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.ui.theme.ChipBg
import com.example.ui.theme.ChipText
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DangerBg
import com.example.ui.theme.DangerText
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.YawarBlue

/**
 * The single pill primitive. Status, count, verified and urgency tags all render
 * through this so the badge family shares one shape, icon size and label style
 * instead of the six variants that had accumulated across screens.
 */
@Composable
fun YawarBadge(
    label: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    iconSize: Dp = 13.dp,
    horizontalPadding: Dp = 8.dp
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = horizontalPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            // Decorative beside a text label, so it carries no description of its own.
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = label, color = contentColor, style = style)
    }
}

/** Semantic shorthand for the tinted tags that are not driven by a status enum. */
enum class YawarBadgeTone { Neutral, Info, Success, Warning, Danger }

@Composable
fun YawarTonalBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: YawarBadgeTone = YawarBadgeTone.Neutral,
    icon: ImageVector? = null
) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (tone) {
        YawarBadgeTone.Neutral -> ChipBg to ChipText
        YawarBadgeTone.Info -> scheme.primaryContainer to scheme.onPrimaryContainer
        YawarBadgeTone.Success -> PaleGreen to DeepGreen
        YawarBadgeTone.Warning -> WarningAmberBg to WarningAmber
        YawarBadgeTone.Danger -> DangerBg to DangerText
    }
    YawarBadge(label = label, background = bg, contentColor = fg, modifier = modifier, icon = icon)
}

@Composable
fun AppointmentStatusBadge(
    status: AppointmentStatus,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        AppointmentStatus.CONFIRMED,
        AppointmentStatus.COMPLETED -> Triple(PaleGreen, DeepGreen, Icons.Default.CheckCircle)
        AppointmentStatus.SUBMITTED,
        AppointmentStatus.UNDER_REVIEW,
        AppointmentStatus.AWAITING_PROVIDER -> Triple(PaleBlue, YawarBlue, Icons.Default.HourglassTop)
        AppointmentStatus.CHECKED_IN,
        AppointmentStatus.IN_CONSULTATION -> Triple(PaleGreen, ClinicalGreen, Icons.Default.CheckCircle)
        AppointmentStatus.CANCELLED -> Triple(DangerBg, DangerText, Icons.Default.Cancel)
        AppointmentStatus.RESCHEDULE_REQUESTED -> Triple(WarningAmberBg, WarningAmber, Icons.Default.Schedule)
    }

    val label = when (language) {
        AppLanguage.ENGLISH -> status.labelEn
        AppLanguage.DARI -> status.labelFa
        AppLanguage.PASHTO -> status.labelPs
    }

    YawarBadge(
        label = label,
        background = bgColor,
        contentColor = textColor,
        modifier = modifier,
        icon = icon
    )
}

@Composable
fun ClaimStatusBadge(
    status: ClaimStatus,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        ClaimStatus.APPROVED,
        ClaimStatus.PAID,
        ClaimStatus.CLOSED -> Triple(PaleGreen, DeepGreen, Icons.Default.CheckCircle)
        ClaimStatus.SUBMITTED,
        ClaimStatus.COMPLETENESS_REVIEW,
        ClaimStatus.CLINICAL_REVIEW -> Triple(PaleBlue, YawarBlue, Icons.Default.HourglassTop)
        ClaimStatus.PARTIALLY_APPROVED -> Triple(WarningAmberBg, WarningAmber, Icons.Default.Pending)
        ClaimStatus.DECLINED -> Triple(DangerBg, DangerText, Icons.Default.Cancel)
        ClaimStatus.DRAFT -> Triple(ChipBg, ChipText, Icons.Default.Schedule)
    }

    val label = when (language) {
        AppLanguage.ENGLISH -> status.labelEn
        AppLanguage.DARI -> status.labelFa
        AppLanguage.PASHTO -> status.labelPs
    }

    YawarBadge(
        label = label,
        background = bgColor,
        contentColor = textColor,
        modifier = modifier,
        icon = icon
    )
}
