package com.example.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DoctorEntity
import com.example.data.local.FacilityEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ServiceItem
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy

@Composable
fun DoctorDetailDialog(
    doctor: DoctorEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onBook: (DoctorEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Doctor Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = YawarNavy
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Doctor portrait display with 4:5 aspect ratio and rounded corners
                val docImageRes = VerifiedImageResources.doctor(doctor.id)
                if (docImageRes != null) {
                    VerifiedProviderImage(
                        imageRes = docImageRes,
                        contentDescription = doctor.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 5f)
                            .clip(MaterialTheme.shapes.large),
                        contentScale = ContentScale.Crop,
                        fallbackText = doctor.name
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (docImageRes == null) {
                        DoctorAvatarBadge(
                            doctorName = doctor.name,
                            specialty = doctor.specialty,
                            size = 56.dp,
                            doctorId = doctor.id
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = ClinicalGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(doctor.specialty, color = YawarBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(doctor.hospitalAffiliation, color = Slate, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DetailItem(label = "Medical License", value = doctor.licenseNo.ifBlank { "MoPH Verified Specialist" })
                DetailItem(label = "Education & Degrees", value = doctor.education)
                DetailItem(label = "Hospital & City", value = "${doctor.hospitalAffiliation} • ${doctor.city}, ${doctor.province}")
                DetailItem(label = "Clinical Practice", value = "${doctor.yearsExperience} Years Experience")
                DetailItem(label = "Consultation Fee", value = "${doctor.consultationFeeAf} (YHCS Cashless Covered)")
                DetailItem(label = "Available Days", value = doctor.weeklySchedule)
                DetailItem(label = "Languages", value = doctor.languages.joinToString(", "))
                if (doctor.bio.isNotBlank()) {
                    DetailItem(label = "About the Doctor", value = doctor.bio)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(PaleGreen)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Accepted", tint = DeepGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified YHCS Network Provider",
                            fontSize = 11.sp,
                            color = DeepGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onBook(doctor) },
                colors = ButtonDefaults.buttonColors(containerColor = YawarBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Make Appointment with ${doctor.name}", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun FacilityDetailDialog(
    facility: FacilityEntity,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hospital Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = YawarNavy
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hospital Logo showcase with ContentScale.Fit
                val logoRes = VerifiedImageResources.hospital(facility.id)
                VerifiedProviderImage(
                    imageRes = logoRes,
                    contentDescription = "${facility.name} logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    fallbackText = facility.name
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = facility.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Ink
                        )
                        Text(
                            text = "${facility.facilityType} • ${facility.province}",
                            color = YawarBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About section with clear text
                Text(
                    text = "About:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )

                Spacer(modifier = Modifier.height(6.dp))

                val aboutText = buildString {
                    append("${facility.name} is an accredited medical healthcare institution in ${facility.province} partnered with Yawar Hamdard Health Consulting Services (YHCS).\n\n")
                    append("It offers high-quality clinical care with key departments including ${facility.departments.joinToString(", ")}. ")
                    append("The hospital features inpatient wards, advanced diagnostics, modern laboratories, and emergency medical services (${if (facility.hasEmergency24h) "24/7 Emergency & Trauma Active" else "Standard Clinic Hours"}).\n\n")
                    append("Address: ${facility.address}\n")
                    append("Reception Phone: ${facility.contactPhone}\n")
                    append("Emergency Desk: ${facility.emergencyPhone}\n")
                    append("YHCS 24/7 Desk: +93 707 438 303\n")
                    append("Accepted Health Programmes: ${facility.acceptedProgrammes.joinToString(", ")}")
                }

                Text(
                    text = aboutText,
                    fontSize = 13.sp,
                    color = Ink,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (facility.contactPhone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${facility.contactPhone.replace(" ", "")}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Desk", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = YawarBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ServiceDetailDialog(
    service: ServiceItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onRequestCoordinator: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.ENGLISH -> service.titleEn
                        AppLanguage.DARI -> service.titleFa
                        AppLanguage.PASHTO -> service.titlePs
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = YawarNavy,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = service.oneSentencePromise,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepGreen,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(service.description, fontSize = 12.sp, color = Ink, lineHeight = 16.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Who it's for:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink)
                Text(service.forWhom, fontSize = 12.sp, color = Slate)

                Spacer(modifier = Modifier.height(12.dp))

                Text("4-Step Operational Pathway:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink)
                service.pathwaySteps.forEachIndexed { idx, stp ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PaleBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 11.sp, color = YawarNavy, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stp, fontSize = 11.sp, color = Ink)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Required Documents:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink)
                service.requiredDocuments.forEach { doc ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Doc", tint = ClinicalGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(doc, fontSize = 11.sp, color = Slate)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRequestCoordinator,
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule / Request Care", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Slate)
        Text(text = value, fontSize = 12.sp, color = Ink)
    }
}
