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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.YawarNavyDark
import com.example.ui.theme.DangerText
import com.example.ui.theme.OnNavyWarning

@Composable
fun YawarTopAppBar(
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    lowBandwidth: Boolean,
    onRoleSelected: (UserRole) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var langMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(YawarNavyDark, YawarNavy)
                )
            )
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
                        tint = OnNavyWarning,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Quick Role Switcher & Language Selector
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role Selector Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), MaterialTheme.shapes.large)
                            .clickable { roleMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val roleIcon = when (currentRole) {
                            UserRole.PATIENT -> Icons.Default.Person
                            UserRole.DOCTOR -> Icons.Default.LocalHospital
                            UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                        }
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = currentRole.displayName,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentRole.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Role",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = role.displayName,
                                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                                            color = if (role == currentRole) YawarNavy else Color.Unspecified
                                        )
                                        Text(
                                            text = "${role.titleFa} | ${role.titlePs}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                        if (onLogout != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Sign Out",
                                        fontWeight = FontWeight.Bold,
                                        color = DangerText
                                    )
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Language Selector Button
                Box {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { langMenuExpanded = true }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Select Language",
                                tint = Color.White,
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
