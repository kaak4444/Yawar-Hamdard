package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Verified
import com.example.ui.common.YawarBadgeTone
import com.example.ui.common.YawarEmptyState
import com.example.ui.common.YawarTonalBadge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppointmentEntity
import com.example.data.local.ClaimEntity
import com.example.data.local.DoctorEntity
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.AmberText
import com.example.ui.theme.DangerBg
import com.example.ui.theme.DangerText
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: YawarViewModel,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val pendingAppointments = appointments.filter { it.status == AppointmentStatus.SUBMITTED || it.status == AppointmentStatus.UNDER_REVIEW }
    val pendingClaims = claims.filter { it.status == ClaimStatus.SUBMITTED || it.status == ClaimStatus.COMPLETENESS_REVIEW }
    val pendingDoctors = doctors.filter { !it.isVerified }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Coordinator Console Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, BorderColor)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Console",
                            tint = YawarBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YHCS Operations Console",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaleBlue)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("24/7 Operations Desk", color = YawarBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Coordination Desk: +93 707 438 303 • callcenter@yawarconsulting.com",
                    color = Slate,
                    fontSize = 11.sp
                )
            }
        }

        // Actionable Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricTile(
                title = "Queue",
                value = pendingAppointments.size.toString(),
                color = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            AdminMetricTile(
                title = "Claims",
                value = pendingClaims.size.toString(),
                color = YawarBlue,
                modifier = Modifier.weight(1f)
            )
            AdminMetricTile(
                title = "Doctors",
                value = doctors.size.toString(),
                color = DeepGreen,
                modifier = Modifier.weight(1f)
            )
            AdminMetricTile(
                title = "Pending Doc",
                value = pendingDoctors.size.toString(),
                color = if (pendingDoctors.isNotEmpty()) DangerText else ClinicalGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Tab Row: Intake Queue | Claims Review | Doctor Verification
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = YawarNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ClinicalGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Intake Queue (${pendingAppointments.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Claims (${pendingClaims.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        "Verification (${pendingDoctors.size})",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
        }

        // List Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Intake Queue
                    if (pendingAppointments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            YawarEmptyState(
                                title = "Intake queue clear",
                                message = "No pending bookings are waiting for coordination.",
                                icon = Icons.Default.Inbox
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingAppointments) { appt ->
                                AdminAppointmentItem(
                                    appointment = appt,
                                    onConfirm = { viewModel.adminAssignAndConfirm(appt.id, "Farhad (Care Officer)") },
                                    onReschedule = { viewModel.adminRequestReschedule(appt.id, "25 Sep 2026", "11:30 AM") }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Claims Desk
                    if (pendingClaims.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            YawarEmptyState(
                                title = "Claims desk clear",
                                message = "No pending claims are waiting for review.",
                                icon = Icons.Default.Assignment
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingClaims) { clm ->
                                AdminClaimItem(
                                    claim = clm,
                                    onApprove = { viewModel.adminApproveClaim(clm.id) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Doctor Credential Verification
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(doctors) { doc ->
                            AdminDoctorVerificationItem(
                                doctor = doc,
                                onVerify = { viewModel.verifyDoctor(doc.id, true) },
                                onRevoke = { viewModel.verifyDoctor(doc.id, false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(title, fontSize = 11.sp, color = Slate)
        }
    }
}

@Composable
fun AdminDoctorVerificationItem(
    doctor: DoctorEntity,
    onVerify: () -> Unit,
    onRevoke: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(if (doctor.isVerified) PaleGreen else WarningAmberBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (doctor.isVerified) Icons.Default.Verified else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (doctor.isVerified) DeepGreen else AmberText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(doctor.name, fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp)
                        Text("${doctor.specialty} • ${doctor.hospitalAffiliation}", color = YawarBlue, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (doctor.isVerified) PaleGreen else WarningAmberBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = doctor.verificationStatus,
                        color = if (doctor.isVerified) DeepGreen else AmberText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("License: ${doctor.licenseNo}", fontSize = 12.sp, color = Ink)
            Text("Education: ${doctor.education}", fontSize = 11.sp, color = Slate)
            Text("Location: ${doctor.city}, ${doctor.province} Province", fontSize = 11.sp, color = Slate)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!doctor.isVerified) {
                    Button(
                        onClick = onVerify,
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen),
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Verify Credentials", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onRevoke,
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revoke Verification", fontSize = 11.sp, color = DangerText)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentItem(
    appointment: AppointmentEntity,
    onConfirm: () -> Unit,
    onReschedule: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${appointment.id} • ${appointment.province}",
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy,
                    fontSize = 13.sp
                )
                YawarTonalBadge(
                    label = appointment.urgency,
                    tone = if (appointment.urgency == "Urgent") YawarBadgeTone.Danger else YawarBadgeTone.Info
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Patient: ${appointment.patientName} (${appointment.patientPhone})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
            Text("Request: ${appointment.specialty} at ${appointment.facilityName}", fontSize = 12.sp, color = Slate)
            Text("Provider: ${appointment.doctorName.ifBlank { "First Available Specialist" }}", fontSize = 12.sp, color = YawarBlue)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReschedule,
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Propose Alt Slot", fontSize = 11.sp, color = YawarBlue)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen),
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Assign & Confirm", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminClaimItem(
    claim: ClaimEntity,
    onApprove: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${claim.id} • ${claim.claimType}",
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy,
                    fontSize = 13.sp
                )
                Text(
                    text = "${claim.invoiceAmountAf.toInt()} AFN",
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Provider: ${claim.providerOrHospital}", fontSize = 12.sp, color = Slate)
            Text("Patient: ${claim.patientName}", fontSize = 12.sp, color = Ink)
            Text("Notes: ${claim.remarks}", fontSize = 11.sp, color = Slate)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Approve Cashless Settlement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
