package com.example.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.AmberIcon
import com.example.ui.theme.ChipText
import com.example.ui.theme.DangerText
import com.example.ui.theme.GreenSoft
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.SkySoft
import com.example.ui.theme.WarningAmberBg

/**
 * Official Yawar Hamdard brand logo image displaying the packaged untouched source image.
 * Uses ContentScale.Fit without distortion or circular clipping.
 */
@Composable
fun YawarLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Image(
        painter = painterResource(id = VerifiedImageResources.yawarLogo),
        contentDescription = "Yawar Hamdard",
        modifier = modifier
            .widthIn(max = 220.dp)
            .heightIn(max = 96.dp),
        contentScale = ContentScale.Fit
    )
}

/**
 * Large Brand Cover component displaying the official logo as a wide card.
 */
@Composable
fun YawarLogoCover(
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = VerifiedImageResources.yawarLogo),
                contentDescription = "Yawar Hamdard",
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .heightIn(max = 96.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Health Consulting Services • Afghanistan",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Header lockup with Logo + Brand Title
 */
@Composable
fun YawarBrandHeader(
    modifier: Modifier = Modifier,
    textColor: Color = YawarBlue,
    subtextColor: Color = ChipText
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = VerifiedImageResources.yawarLogo),
            contentDescription = "Yawar Hamdard",
            modifier = Modifier
                .widthIn(max = 120.dp)
                .height(40.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Yawar Hamdard",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 19.sp
                )
            )
            Text(
                text = "Health Consulting Services",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = subtextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Hospital Logo Badge:
 * Always maps to verified packaged logo or displays neutral text placeholder.
 */
@Composable
fun HospitalLogoBadge(
    hospitalName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    logoUrl: String = "",
    hospitalId: String = ""
) {
    val idToQuery = logoUrl.ifBlank { hospitalId }.ifBlank { hospitalName }
    val logoRes = VerifiedImageResources.hospital(idToQuery)
    val imageModifier = modifier
        .size(size)
        .clip(MaterialTheme.shapes.small)
        .border(1.dp, BorderColor, MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surface)
    if (logoRes == null && idToQuery.startsWith("https://", ignoreCase = true)) {
        AsyncImage(
            model = idToQuery,
            contentDescription = "$hospitalName logo",
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    } else {
        VerifiedProviderImage(
            imageRes = logoRes,
            contentDescription = "$hospitalName logo",
            modifier = imageModifier,
            contentScale = ContentScale.Fit,
            fallbackText = hospitalName
        )
    }
}

/**
 * Doctor Profile Avatar Badge:
 * Displays verified packaged portrait with consistent 4:5 ratio or fallback initials.
 */
@Composable
fun DoctorAvatarBadge(
    doctorName: String,
    specialty: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    imageUrl: String = "",
    doctorId: String = ""
) {
    val imageRes = VerifiedImageResources.doctor(doctorId).let { it ?: VerifiedImageResources.doctor(imageUrl) }

    if (imageRes != null) {
        VerifiedProviderImage(
            imageRes = imageRes,
            contentDescription = doctorName,
            modifier = modifier
                .size(size)
                .aspectRatio(4f / 5f)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
            fallbackText = doctorName
        )
    } else if (imageUrl.startsWith("https://", ignoreCase = true)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = doctorName,
            modifier = modifier.size(size).aspectRatio(4f / 5f).clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    } else {
        val (bgColor, tintColor) = when {
            specialty.contains("Sono", ignoreCase = true) -> Pair(SkySoft, YawarBlue)
            specialty.contains("Cardio", ignoreCase = true) -> Pair(Color(0xFFFEE2E2), DangerText)
            specialty.contains("Pediat", ignoreCase = true) -> Pair(WarningAmberBg, AmberIcon)
            specialty.contains("Neuro", ignoreCase = true) -> Pair(PurpleSoft, PurpleAccent)
            specialty.contains("Ortho", ignoreCase = true) -> Pair(GreenSoft, ClinicalGreen)
            else -> Pair(PaleBlue, YawarBlue)
        }

        val initials = doctorName
            .replace("Dr.", "")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first() }
            .joinToString("")
            .ifEmpty { "DR" }

        Box(
            modifier = modifier
                .size(size)
                .clip(MaterialTheme.shapes.medium)
                .background(bgColor)
                .border(1.5.dp, tintColor.copy(alpha = 0.35f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
        }
    }
}
