package com.example.ui.doctor

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.AppointmentStatus
import com.example.ui.common.AppointmentStatusBadge
import com.example.ui.common.YawarBadgeTone
import com.example.ui.common.YawarEmptyState
import com.example.ui.common.YawarTonalBadge
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.ChipBg
import com.example.ui.theme.DangerBg
import com.example.ui.theme.OnNavyMuted
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun DoctorDashboardScreen(
    viewModel: YawarViewModel,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val activeDoctor = doctors.firstOrNull { it.id == "doc_momand" } ?: doctors.firstOrNull()

    // Dialog state for declining or completing
    var completeApptId by remember { mutableStateOf<String?>(null) }
    var completeSummaryText by remember { mutableStateOf("") }

    val pendingRequests = appointments.filter { it.status == AppointmentStatus.SUBMITTED || it.status == AppointmentStatus.UNDER_REVIEW }
    val confirmedVisits = appointments.filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.CHECKED_IN }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Doctor Verified Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(YawarNavy)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PaleGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Doctor Icon",
                        tint = DeepGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeDoctor?.name ?: "Dr. Abdul Wasi Momand",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified License",
                            tint = ClinicalGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${activeDoctor?.specialty ?: "Sonology"} • ${activeDoctor?.hospitalAffiliation ?: "Al-Hayat Hospital"} • Lic: ${activeDoctor?.licenseNo ?: "MoPH-AF-KBL-2016-1042"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnNavyMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Tab Row: Pending Requests (X) | Confirmed Visits (Y)
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
                        "Appointment Queue (${pendingRequests.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) YawarNavy else Slate
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Confirmed Visits (${confirmedVisits.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) YawarNavy else Slate
                    )
                }
            )
        }

        // List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (selectedTab == 0) {
                if (pendingRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        YawarEmptyState(
                            title = "No pending requests",
                            message = "Consultation referrals awaiting your review will appear here.",
                            icon = Icons.Default.Inbox
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingRequests) { appt ->
                            DoctorPendingRequestCard(
                                appointment = appt,
                                onAccept = { viewModel.doctorConfirmAppointment(appt.id) },
                                onDecline = { viewModel.doctorDeclineAppointment(appt.id, "Time conflict; please reschedule.") }
                            )
                        }
                    }
                }
            } else {
                if (confirmedVisits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No confirmed visits scheduled for today.", color = Slate, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(confirmedVisits) { appt ->
                            DoctorConfirmedVisitCard(
                                appointment = appt,
                                onMarkComplete = { completeApptId = appt.id }
                            )
                        }
                    }
                }
            }
        }

        // Complete Visit Dialog
        if (completeApptId != null) {
            AlertDialog(
                onDismissRequest = { completeApptId = null },
                title = { Text("Complete Visit & Clinical Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column {
                        Text("Enter clinical findings, prescriptions or follow-up note:", fontSize = 12.sp, color = Slate)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = completeSummaryText,
                            onValueChange = { completeSummaryText = it },
                            placeholder = { Text("e.g. ECG normal. Prescribed Atenolol 25mg daily. Review in 4 weeks.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            completeApptId?.let { id ->
                                viewModel.doctorCompleteVisit(id, completeSummaryText.ifBlank { "Consultation finished normally." })
                            }
                            completeApptId = null
                            completeSummaryText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen)
                    ) {
                        Text("Save & Complete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { completeApptId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DoctorPendingRequestCard(
    appointment: AppointmentEntity,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
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
                    text = appointment.id,
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy,
                    fontSize = 13.sp
                )
                YawarTonalBadge(
                    label = "Urgency: ${appointment.urgency}",
                    tone = if (appointment.urgency == "Urgent") YawarBadgeTone.Danger else YawarBadgeTone.Info
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Patient: ${appointment.patientName}",
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 16.sp
            )

            Text(
                text = "Reason: ${appointment.reasonForCare}",
                color = Slate,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(ChipBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Slot", tint = YawarNavy, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.appointmentDate} • ${appointment.timeSlot} (${appointment.visitType.labelEn})",
                    fontSize = 11.sp,
                    color = Ink,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Decline", color = EmergencyRed, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen),
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Accept & Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DoctorConfirmedVisitCard(
    appointment: AppointmentEntity,
    onMarkComplete: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
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
                    text = appointment.id,
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(PaleGreen)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Confirmed",
                        color = DeepGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Patient: ${appointment.patientName}",
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 16.sp
            )

            Text(
                text = "${appointment.appointmentDate} • ${appointment.timeSlot}",
                color = Slate,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onMarkComplete,
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark Completed & Add Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
