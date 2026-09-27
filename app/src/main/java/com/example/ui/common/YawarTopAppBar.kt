package com.example.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.BorderColor
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.DangerText

@Composable
fun YawarTopAppBar(
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    lowBandwidth: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null
) {
    var langMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, BorderColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand lockup. The image carries the "Yawar Hamdard / Health
            // Consulting Services" wordmark, so the separate text labels that
            // sat beside the old mark-only asset are gone.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = VerifiedImageResources.yawarLogoLockup),
                    contentDescription = "Yawar Hamdard Health Consulting Services",
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(max = 150.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentScale = ContentScale.Fit
                )
                if (lowBandwidth) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Low-Bandwidth Mode",
                        tint = YawarBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Server-assigned role and language selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(PaleBlue)
                        .border(1.dp, BorderColor, MaterialTheme.shapes.large)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val roleIcon = when (currentRole) {
                        UserRole.PATIENT -> Icons.Default.Person
                        UserRole.DOCTOR -> Icons.Default.LocalHospital
                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    }
                    Icon(roleIcon, contentDescription = currentRole.displayName, tint = YawarBlue, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(currentRole.displayName, color = YawarNavy, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (onLogout != null) {
                    Text(
                        text = "Sign out",
                        color = DangerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onLogout() }.padding(horizontal = 4.dp)
                    )
                }

                // Language Selector Button
                Box {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PaleBlue)
                            .border(1.dp, BorderColor, CircleShape)
                            .clickable { langMenuExpanded = true }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Select Language",
                                tint = YawarBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = langMenuExpanded,
                        onDismissRequest = { langMenuExpanded = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = lang.displayName,
                                            fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                            color = if (lang == currentLanguage) ClinicalGreen else Color.Unspecified
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    onLanguageSelected(lang)
                                    langMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
