package com.example.ui.patient

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.data.model.AppLanguage
import com.example.data.model.AppointmentStatus
import com.example.ui.common.AppointmentStatusBadge
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.AmberIcon
import com.example.ui.theme.AmberText
import com.example.ui.theme.DangerText
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun AppointmentsScreen(
    viewModel: YawarViewModel,
    onBookNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    var statusFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Confirmed", "Under Review", "Completed", "Cancelled")

    val filteredList = appointments.filter { appt ->
        when (statusFilter) {
            "Confirmed" -> appt.status == AppointmentStatus.CONFIRMED
            "Under Review" -> appt.status == AppointmentStatus.UNDER_REVIEW || appt.status == AppointmentStatus.SUBMITTED
            "Completed" -> appt.status == AppointmentStatus.COMPLETED
            "Cancelled" -> appt.status == AppointmentStatus.CANCELLED
            else -> true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Appointments",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                fontSize = 18.sp
                            )
                        )

                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .background(PaleBlue)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${appointments.size} Total",
                                color = YawarNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(filterOptions) { opt ->
                            FilterChip(
                                selected = statusFilter == opt,
                                onClick = { statusFilter = opt },
                                label = { Text(opt, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PaleGreen,
                                    selectedLabelColor = DeepGreen
                                )
                            )
                        }
                    }
                }
            }

            // Appointments List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Appointments",
                            tint = Slate,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No appointments found under '$statusFilter'",
                            color = Slate,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBookNew,
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen)
                        ) {
                            Text("Book First Appointment")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { appt ->
                        AppointmentDetailCard(
                            appointment = appt,
                            language = language,
                            onReschedule = {
                                viewModel.adminRequestReschedule(appt.id, "Thursday, 25 Sep 2026", "11:30 AM")
                            },
                            onCancel = {
                                viewModel.doctorDeclineAppointment(appt.id, "Cancelled by patient via app.")
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Book New
        FloatingActionButton(
            onClick = onBookNew,
            containerColor = ClinicalGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 16.dp, 16.dp, 80.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Book Appointment")
        }
    }
}

@Composable
fun AppointmentDetailCard(
    appointment: AppointmentEntity,
    language: AppLanguage,
    onReschedule: () -> Unit,
    onCancel: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appointment.id,
                        fontWeight = FontWeight.Bold,
                        color = YawarNavy,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${appointment.specialty}",
                        color = Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AppointmentStatusBadge(status = appointment.status, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appointment.doctorName.ifBlank { "First Available Specialist" },
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 16.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = "Facility", tint = Slate, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${appointment.facilityName} (${appointment.province})",
                    color = Slate,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(PaleBlue)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Date", tint = YawarBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.appointmentDate} • ${appointment.timeSlot} (${appointment.visitType.labelEn})",
                    color = YawarNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (appointment.isDependent) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scheduled for: ${appointment.dependentName} (${appointment.dependentRelation})",
                    color = DeepGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (appointment.doctorPreparationNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(Color(0xFFFFFBEB))
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Preparation Note", tint = AmberIcon, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Patient prep: ${appointment.doctorPreparationNotes}",
                        color = AmberText,
                        fontSize = 11.sp
                    )
                }
            }

            if (appointment.coordinatorNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Coordinator note: ${appointment.coordinatorNotes}",
                    color = Slate,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row if appointment is active
            if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.UNDER_REVIEW) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReschedule,
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Request Reschedule", fontSize = 11.sp, color = YawarBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        shape = MaterialTheme.shapes.extraSmall,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, color = DangerText)
                    }
                }
            }
        }
    }
}
