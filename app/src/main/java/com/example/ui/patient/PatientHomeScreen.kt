package com.example.ui.patient

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.DoctorEntity
import com.example.data.local.FacilityEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ServiceItem
import com.example.ui.common.AppStrings
import com.example.ui.common.DoctorAvatarBadge
import com.example.ui.common.EmergencyCard
import com.example.ui.common.YawarSectionHeader
import com.example.ui.common.HospitalLogoBadge
import com.example.ui.common.VerifiedImageResources
import com.example.ui.common.VerifiedProviderImage
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.YawarSpacing
import com.example.ui.theme.DangerBg
import com.example.ui.theme.DangerText
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.SkySoft
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun PatientHomeScreen(
    viewModel: YawarViewModel,
    onNavigateToFindCare: (initialTab: Int) -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToClaims: () -> Unit,
    onNavigateToCases: () -> Unit,
    onOpenBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val doctors by viewModel.filteredDoctors.collectAsState()
    val facilities by viewModel.filteredFacilities.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sampleBimaIds = listOf("BIMA-26-0001", "BIMA-26-0002", "BIMA-26-0003", "BIMA-26-0004")

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Gray canvas so white cards separate
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Emergency Hotline Alert
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                EmergencyCard(language = language)
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bima Insurance IDs",
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "SAMPLE",
                            color = YawarBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(PaleBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Example IDs only — use registered member details for real coverage.",
                        color = Slate,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )
                    sampleBimaIds.chunked(2).forEach { idRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            idRow.forEach { bimaId ->
                                Text(
                                    text = bimaId,
                                    color = YawarBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(PaleBlue)
                                        .padding(horizontal = 10.dp, vertical = 9.dp)
                                )
                            }
                        }
                        if (idRow != sampleBimaIds.take(2)) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // 4. Quick Actions
        item {
            Column(modifier = Modifier.padding(horizontal = YawarSpacing.lg, vertical = YawarSpacing.sm + 2.dp)) {
                YawarSectionHeader(title = "Quick Actions")

                Spacer(modifier = Modifier.height(YawarSpacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = AppStrings.getBookAppointment(language),
                        icon = Icons.Default.CalendarMonth,
                        iconTint = ClinicalGreen,
                        iconBg = PaleGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBooking
                    )
                    QuickActionTile(
                        title = AppStrings.getFindDoctor(language),
                        icon = Icons.Default.Person,
                        iconTint = YawarBlue,
                        iconBg = PaleBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToFindCare(0) }
                    )
                    QuickActionTile(
                        title = AppStrings.getFindHospital(language),
                        icon = Icons.Default.LocalHospital,
                        iconTint = PurpleAccent,
                        iconBg = PurpleSoft,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToFindCare(1) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Care Services",
                        icon = Icons.Default.MedicalServices,
                        iconTint = Color(0xFF0284C7),
                        iconBg = SkySoft,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToFindCare(2) }
                    )
                    QuickActionTile(
                        title = AppStrings.getClaimsAndCashless(language),
                        icon = Icons.Default.ReceiptLong,
                        iconTint = DeepGreen,
                        iconBg = PaleGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClaims
                    )
                    QuickActionTile(
                        title = AppStrings.getCoordinationCases(language),
                        icon = Icons.Default.HealthAndSafety,
                        iconTint = YawarNavy,
                        iconBg = PaleBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCases
                    )
                }
            }
        }

        // 5. Light partner-care card with a small blue accent
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Decorative background icon
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = YawarBlue.copy(alpha = 0.06f),
                            modifier = Modifier
                                .size(180.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 40.dp, y = 20.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(PaleBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = YawarBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Core Coordination Desk",
                                            fontWeight = FontWeight.Bold,
                                            color = Ink,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Active YHCS Corporate Coverage",
                                            color = Slate,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(PaleBlue)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = YawarBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Direct access to top medical specialists, cashless inpatient admissions, and 24/7 care coordination throughout Afghanistan.",
                                color = Slate,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onOpenBooking,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = YawarBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text(
                                        text = "Get Started",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+93707438303"))
                                        context.startActivity(callIntent)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = YawarBlue
                                    ),
                                    border = BorderStroke(1.5.dp, YawarBlue),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Call 24/7",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. DOCTOR PROFILES SECTION (Requested by user: "in teh home there should not be upcoming appoinment, ther eshould be the profile of the doctors to make the home filled an lookin better")
        item {
            Column(modifier = Modifier.padding(horizontal = YawarSpacing.lg, vertical = YawarSpacing.md)) {
                YawarSectionHeader(
                    title = "Doctor Profiles & Specialists",
                    subtitle = "Browse doctors. Yawar coordinators arrange appointments after triage.",
                    actionLabel = "View All →",
                    onAction = { onNavigateToFindCare(0) }
                )

                Spacer(modifier = Modifier.height(YawarSpacing.md))

                // Display top doctor profiles
                doctors.take(4).forEach { doc ->
                    DoctorListCard(
                        doctor = doc,
                        language = language,
                        onBook = {
                            viewModel.startBooking(specialty = doc.specialty)
                            onOpenBooking()
                        },
                        onClick = { viewModel.openDoctorDetail(doc) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // 7. PARTNER HOSPITALS (With hospital logos next to hospital names as requested)
        item {
            Column(modifier = Modifier.padding(horizontal = YawarSpacing.lg, vertical = YawarSpacing.sm)) {
                YawarSectionHeader(
                    title = "Accredited Hospital Network",
                    subtitle = "Partner facilities with YHCS cashless corporate coverage",
                    actionLabel = "All Hospitals →",
                    onAction = { onNavigateToFindCare(1) }
                )

                Spacer(modifier = Modifier.height(YawarSpacing.md))

                facilities.take(3).forEach { fac ->
                    HospitalListCard(
                        facility = fac,
                        language = language,
                        onClick = { viewModel.openFacilityDetail(fac) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 8. YHCS Core Health Services Catalogue
        item {
            Column(modifier = Modifier.padding(vertical = YawarSpacing.sm + 2.dp)) {
                YawarSectionHeader(
                    title = "YHCS Core Health Services",
                    actionLabel = "Catalogue →",
                    onAction = { onNavigateToFindCare(2) },
                    modifier = Modifier.padding(horizontal = YawarSpacing.lg)
                )

                Spacer(modifier = Modifier.height(YawarSpacing.md))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.services) { service ->
                        ServiceCardHome(
                            service = service,
                            language = language,
                            onClick = { viewModel.openServiceDetail(service) }
                        )
                    }
                }
            }
        }

        // 9. Operational Care Assistance Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "YHCS Care Assistance Desk",
                                fontWeight = FontWeight.Bold,
                                color = YawarNavy,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "24/7 Hotline: +93 707 438 303\ninfo@yawarconsulting.com",
                                color = Slate,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Button(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+93707438303"))
                                context.startActivity(callIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YawarBlue,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("Call Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ServiceCardHome(
    service: ServiceItem,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(PaleGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = service.titleEn,
                            tint = DeepGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.ENGLISH -> service.titleEn
                            AppLanguage.DARI -> service.titleFa
                            AppLanguage.PASHTO -> service.titlePs
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = service.oneSentencePromise,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "4-step pathway",
                    color = YawarBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Learn more →",
                    color = ClinicalGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Doctor Profile Card with a care-coordination request action
 */
@Composable
fun DoctorListCard(
    doctor: DoctorEntity,
    language: AppLanguage,
    onBook: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Doctor Avatar Badge (Displays real doctor photo if available)
                DoctorAvatarBadge(
                    doctorName = doctor.name,
                    specialty = doctor.specialty,
                    size = 54.dp,
                    imageUrl = doctor.photoFile,
                    doctorId = doctor.id
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = doctor.name,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            fontSize = 16.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PaleGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Doctor",
                                    tint = ClinicalGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Verified",
                                    color = DeepGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "${doctor.specialty} • ${doctor.yearsExperience} yrs practice",
                        color = YawarBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${doctor.hospitalAffiliation} • ${doctor.city}",
                        color = Slate,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Days: ${doctor.weeklySchedule}",
                        color = Slate,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Fee: ${doctor.consultationFeeAf} (YHCS Covered)",
                        color = Ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YawarBlue,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Request care", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Hospital Card with HospitalLogoBadge next to the hospital name
 */
@Composable
fun HospitalListCard(
    facility: FacilityEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hospital Logo Badge next to the hospital name
            HospitalLogoBadge(
                hospitalName = facility.name,
                size = 52.dp,
                logoUrl = facility.logoFile
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = facility.name,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (facility.hasEmergency24h) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DangerBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "24/7 ER",
                                color = DangerText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${facility.facilityType} • ${facility.province}",
                    color = YawarBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = facility.departments.take(3).joinToString(", "),
                    color = Slate,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
