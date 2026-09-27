package com.example.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ChipBg
import com.example.ui.theme.ChipText
import com.example.ui.theme.DividerSoft

/**
 * Verified offline image resources mapping stable doctor and hospital IDs
 * to packaged drawables in res/drawable-nodpi.
 */
object VerifiedImageResources {

    @DrawableRes
    val yawarLogo: Int = R.drawable.yawar_logo_original

    /** Full horizontal lockup: mark plus "Yawar Hamdard Health Consulting Services" wordmark. */
    @DrawableRes
    val yawarLogoLockup: Int = R.drawable.yhcs_logo_lockup

    /**
     * Maps doctor entity IDs or legacy filenames to local packaged portraits.
     */
    @DrawableRes
    fun doctor(doctorId: String): Int? {
        val clean = doctorId.lowercase().trim()
        return when {
            clean == "doc_momand" || clean.contains("momand") -> R.drawable.doc_momand
            clean == "doc_katwazi" || clean.contains("katwazi") -> R.drawable.doc_katwazi
            clean == "doc_safir" || clean.contains("safir") -> R.drawable.doc_safir
            clean == "doc_shinwari" || clean.contains("shinwari") -> R.drawable.doc_shinwari
            else -> null
        }
    }

    /**
     * Maps hospital entity IDs or legacy filenames to local packaged logos.
     * Note: Missing Blossom logo intentionally returns null to display a neutral text placeholder.
     */
    @DrawableRes
    fun hospital(hospitalId: String): Int? {
        val clean = hospitalId.lowercase().trim()
        // Missing Blossom logo uses a neutral text placeholder, not a fabricated logo.
        if (clean == "hosp_16" || clean.contains("blossom")) {
            return null
        }

        return when {
            clean == "hosp_01" || clean.contains("hayat") -> R.drawable.hosp_01
            clean == "hosp_02" || clean.contains("khalid") -> R.drawable.hosp_02
            clean == "hosp_03" || clean.contains("eltiam") -> R.drawable.hosp_03
            clean == "hosp_04" || clean.contains("nang") -> R.drawable.hosp_04
            clean == "hosp_05" || clean.contains("mohmand") -> R.drawable.hosp_05
            clean == "hosp_06" || clean.contains("abdali") -> R.drawable.hosp_06
            clean == "hosp_07" || clean.contains("ettemaad") -> R.drawable.hosp_07
            clean == "hosp_08" || clean.contains("mirwais") -> R.drawable.hosp_08
            clean == "hosp_09" || clean.contains("aryana") -> R.drawable.hosp_09
            clean == "hosp_10" || clean.contains("zubair") -> R.drawable.hosp_10
            clean == "hosp_11" || clean.contains("kims") -> R.drawable.hosp_11
            clean == "hosp_12" || clean.contains("wakhan") -> R.drawable.hosp_12
            clean == "hosp_13" || clean.contains("shafa") -> R.drawable.hosp_13
            clean == "hosp_14" || clean.contains("timar") -> R.drawable.hosp_14
            clean == "hosp_15" || clean.contains("hasib") -> R.drawable.hosp_15
            clean == "hosp_17" || clean.contains("sarwari") -> R.drawable.hosp_17
            else -> null
        }
    }
}

/**
 * Standard provider image composable rendering offline packaged images via painterResource,
 * or displaying a clean neutral text placeholder when no logo is available (e.g., Blossom Healthcare Center).
 */
@Composable
fun VerifiedProviderImage(
    @DrawableRes imageRes: Int?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    fallbackText: String = contentDescription
) {
    if (imageRes != null) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Neutral text placeholder, not a fabricated logo
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.small)
                .background(ChipBg)
                .border(1.dp, DividerSoft, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText.ifBlank { "Facility" },
                color = ChipText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
