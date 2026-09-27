package com.example.ui.patient

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.VisitType
import com.example.ui.common.AppStrings
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
import com.example.ui.theme.DangerBg
import com.example.ui.viewmodel.YawarViewModel

@Composable
fun BookingStepperScreen(
    viewModel: YawarViewModel,
    onClose: () -> Unit,
    onViewAppointments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step by viewModel.bookingStep.collectAsState()
    val draft by viewModel.bookingDraft.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val lastRef by viewModel.lastSubmittedReference.collectAsState()

    val totalSteps = 8

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Stepper Navigation Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Booking",
                            tint = Ink
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (step <= totalSteps) "Care Coordination Stepper" else "Booking Confirmation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        )
                        if (step <= totalSteps) {
                            Text(
                                text = "Step $step of $totalSteps • YHCS Afghanistan",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = YawarBlue,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                if (step <= totalSteps) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .background(PaleGreen)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(step * 100) / totalSteps}%",
                            color = DeepGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Stepper Progress Indicators
        if (step <= totalSteps) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    i < step -> ClinicalGreen
                                    i == step -> YawarNavy
                                    else -> BorderColor
                                }
                            )
                    )
                }
            }
        }

        // Stepper Body
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (step) {
                1 -> Step1Who(draft = draft, onUpdate = viewModel::updateBookingDraft)
                2 -> Step2WhatCare(draft = draft, onUpdate = viewModel::updateBookingDraft)
                3 -> Step3Where(draft = draft, facilities = facilities, onUpdate = viewModel::updateBookingDraft)
                4 -> Step4WhoDoctor(draft = draft, doctors = doctors, onUpdate = viewModel::updateBookingDraft)
                5 -> Step5When(draft = draft, onUpdate = viewModel::updateBookingDraft)
                6 -> Step6Coverage(draft = draft, onUpdate = viewModel::updateBookingDraft)
                7 -> Step7ContactPref(draft = draft, onUpdate = viewModel::updateBookingDraft)
                8 -> Step8ReviewConsent(draft = draft, language = language, onUpdate = viewModel::updateBookingDraft)
                9 -> Step9Confirmation(
                    referenceId = lastRef ?: "YHCS-2026-9921",
                    draft = draft,
                    language = language,
                    onViewAppointments = onViewAppointments
                )
            }
        }

        // Bottom Stepper Action Buttons
        if (step <= totalSteps) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevBookingStep() },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back", color = Ink)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Button(
                        onClick = {
                            if (step == totalSteps) {
                                viewModel.submitBooking()
                            } else {
                                viewModel.nextBookingStep()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (step == totalSteps) ClinicalGreen else YawarNavy,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (step == totalSteps) "Submit Request" else "Next Step →",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Step 1: Who is the appointment for?
@Composable
private fun Step1Who(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    Column {
        Text(
            text = "1. Patient Verification",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Appointments are scheduled strictly for the primary registered patient.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PaleGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Patient",
                            tint = DeepGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ahmad Shah", fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = ClinicalGreen, modifier = Modifier.size(16.dp))
                        }
                        Text("+93 70 123 4567 • Primary Account Holder", fontSize = 12.sp, color = Slate)
                        Text("Member ID: YHCS-CORP-9021", fontSize = 12.sp, color = YawarBlue, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(PaleBlue)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Policy Notice",
                            tint = YawarNavy,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified Account Holder: Ahmad Shah. For security and clinical accuracy, medical consultations are strictly restricted to the primary registered patient.",
                            fontSize = 12.sp,
                            color = YawarNavy,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// Step 2: What care is needed?
@Composable
private fun Step2WhatCare(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    val specialties = listOf("Cardiology", "Orthopedics & Trauma", "Pediatrics", "Internal Medicine", "General Surgery", "Neurology")
    val urgencies = listOf("Routine", "Priority", "Urgent")

    Column {
        Text(
            text = "2. What care is needed?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Specify the clinical discipline, urgency, and core reason for consultation.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Medical Specialty", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            specialties.take(3).forEach { spec ->
                FilterChip(
                    selected = draft.specialty == spec,
                    onClick = { onUpdate { it.copy(specialty = spec) } },
                    label = { Text(spec, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleGreen)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            specialties.drop(3).forEach { spec ->
                FilterChip(
                    selected = draft.specialty == spec,
                    onClick = { onUpdate { it.copy(specialty = spec) } },
                    label = { Text(spec, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleGreen)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Urgency Level", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            urgencies.forEach { urg ->
                FilterChip(
                    selected = draft.urgency == urg,
                    onClick = { onUpdate { it.copy(urgency = urg) } },
                    label = { Text(urg, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (urg == "Urgent") DangerBg else PaleBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = draft.reasonForCare,
            onValueChange = { reason -> onUpdate { it.copy(reasonForCare = reason) } },
            label = { Text("Primary Reason for Care / Chief Complaint") },
            placeholder = { Text("e.g. Chest tightness on exertion, shortness of breath") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = draft.symptomsSummary,
            onValueChange = { sym -> onUpdate { it.copy(symptomsSummary = sym) } },
            label = { Text("Symptoms & Duration (Optional)") },
            placeholder = { Text("e.g. Started 2 weeks ago, worse in morning") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

// Step 3: Where?
@Composable
private fun Step3Where(
    draft: com.example.ui.viewmodel.BookingDraft,
    facilities: List<com.example.data.local.FacilityEntity>,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    val provinces = listOf("Kabul", "Herat", "Balkh", "Kandahar", "Nangarhar")

    Column {
        Text(
            text = "3. Where should care be provided?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Select your province, preferred hospital facility, and visit format.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Visit Format", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisitType.values().forEach { vType ->
                FilterChip(
                    selected = draft.visitType == vType,
                    onClick = { onUpdate { it.copy(visitType = vType) } },
                    label = { Text(vType.labelEn, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleBlue)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Province", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            provinces.take(3).forEach { prov ->
                FilterChip(
                    selected = draft.province == prov,
                    onClick = { onUpdate { it.copy(province = prov) } },
                    label = { Text(prov, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Preferred Hospital / Medical Center", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))

        facilities.forEach { fac ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onUpdate { it.copy(facilityId = fac.id, facilityName = fac.name) }
                    },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (draft.facilityName == fac.name) PaleBlue else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (draft.facilityName == fac.name) YawarBlue else BorderColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = draft.facilityName == fac.name,
                        onClick = { onUpdate { it.copy(facilityId = fac.id, facilityName = fac.name) } },
                        colors = RadioButtonDefaults.colors(selectedColor = YawarBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(fac.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                        Text("${fac.facilityType} • Approx. ${fac.distanceKm} km", fontSize = 11.sp, color = Slate)
                    }
                }
            }
        }
    }
}

// Step 4: Who?
@Composable
private fun Step4WhoDoctor(
    draft: com.example.ui.viewmodel.BookingDraft,
    doctors: List<com.example.data.local.DoctorEntity>,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    Column {
        Text(
            text = "4. Select Provider",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Choose a specific verified consultant or allow YHCS to assign the first suitable specialist.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // First Suitable Doctor Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onUpdate {
                        it.copy(
                            isFirstSuitableDoctor = true,
                            doctorId = "",
                            doctorName = "First Available Specialist"
                        )
                    }
                },
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = if (draft.isFirstSuitableDoctor) PaleGreen else Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (draft.isFirstSuitableDoctor) ClinicalGreen else BorderColor
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = draft.isFirstSuitableDoctor,
                    onClick = {
                        onUpdate {
                            it.copy(
                                isFirstSuitableDoctor = true,
                                doctorId = "",
                                doctorName = "First Available Specialist"
                            )
                        }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = ClinicalGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "First Suitable Verified Doctor",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 14.sp
                    )
                    Text(
                        "Fastest confirmation • YHCS Coordinator matches nearest available doctor",
                        fontSize = 12.sp,
                        color = Slate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Or Choose a Specific Specialist", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))

        doctors.filter { it.specialty.contains(draft.specialty, ignoreCase = true) || draft.specialty == "Cardiology" }
            .take(3).forEach { doc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onUpdate {
                                it.copy(
                                    isFirstSuitableDoctor = false,
                                    doctorId = doc.id,
                                    doctorName = doc.name
                                )
                            }
                        },
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = if (!draft.isFirstSuitableDoctor && draft.doctorId == doc.id) PaleBlue else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!draft.isFirstSuitableDoctor && draft.doctorId == doc.id) YawarBlue else BorderColor
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !draft.isFirstSuitableDoctor && draft.doctorId == doc.id,
                            onClick = {
                                onUpdate {
                                    it.copy(
                                        isFirstSuitableDoctor = false,
                                        doctorId = doc.id,
                                        doctorName = doc.name
                                    )
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = YawarBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(doc.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                            Text("${doc.subspecialty} • ${doc.hospitalAffiliation}", fontSize = 11.sp, color = Slate)
                        }
                    }
                }
            }
    }
}

// Step 5: When?
@Composable
private fun Step5When(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    val dates = listOf("Tomorrow, 23 Sep 2026", "Thursday, 24 Sep 2026", "Saturday, 26 Sep 2026", "Sunday, 27 Sep 2026")
    val slots = listOf("09:30 AM", "11:00 AM", "02:30 PM", "04:00 PM")

    Column {
        Text(
            text = "5. Date and Time Slot",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Select preferred consultation date and available facility working hours.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        dates.forEach { dt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onUpdate { it.copy(appointmentDate = dt) } },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (draft.appointmentDate == dt) PaleBlue else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (draft.appointmentDate == dt) YawarBlue else BorderColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Date",
                        tint = if (draft.appointmentDate == dt) YawarBlue else Slate,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(dt, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Time Slot", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slots.take(2).forEach { slot ->
                FilterChip(
                    selected = draft.timeSlot == slot,
                    onClick = { onUpdate { it.copy(timeSlot = slot) } },
                    label = { Text(slot, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleGreen)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slots.drop(2).forEach { slot ->
                FilterChip(
                    selected = draft.timeSlot == slot,
                    onClick = { onUpdate { it.copy(timeSlot = slot) } },
                    label = { Text(slot, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleGreen)
                )
            }
        }
    }
}

// Step 6: Coverage and documents
@Composable
private fun Step6Coverage(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    Column {
        Text(
            text = "6. Coverage and Documents",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Provide membership details and upload relevant diagnostic reports or prior prescriptions.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = draft.corporateMemberId,
            onValueChange = { mem -> onUpdate { it.copy(corporateMemberId = mem) } },
            label = { Text("YHCS / Corporate Health Member ID") },
            placeholder = { Text("e.g. YHCS-CORP-9021") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Attached Clinical Documents", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Document Attached",
                        tint = YawarBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prior_Clinical_Summary_Aug2026.pdf",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Ink
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onUpdate {
                            it.copy(attachedDocs = it.attachedDocs + "Additional_Prescription.jpg")
                        }
                    },
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Add File", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Attach Lab Report or Prescription", fontSize = 12.sp)
                }
            }
        }
    }
}

// Step 7: Contact preference
@Composable
private fun Step7ContactPref(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    val prefs = listOf("WhatsApp & In-App", "Phone Call Only", "Email & SMS")

    Column {
        Text(
            text = "7. Contact Preference",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "How should YHCS coordinators deliver updates and appointment reminders?",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        prefs.forEach { pref ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onUpdate { it.copy(contactPreference = pref) } },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (draft.contactPreference == pref) PaleGreen else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (draft.contactPreference == pref) ClinicalGreen else BorderColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = draft.contactPreference == pref,
                        onClick = { onUpdate { it.copy(contactPreference = pref) } },
                        colors = RadioButtonDefaults.colors(selectedColor = ClinicalGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pref, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(PaleBlue)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Notice", tint = YawarNavy, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "A minimal-data alert is dispatched to callcenter@yawarconsulting.com upon submission.",
                    fontSize = 11.sp,
                    color = YawarNavy
                )
            }
        }
    }
}

// Step 8: Review and Consent
@Composable
private fun Step8ReviewConsent(
    draft: com.example.ui.viewmodel.BookingDraft,
    language: AppLanguage,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    Column {
        Text(
            text = "8. Review and Consent",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Confirm all details before submitting to the YHCS Care Desk.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ReviewRow(label = "Patient", value = "Ahmad Shah (Primary Patient)")
                ReviewRow(label = "Specialty", value = draft.specialty)
                ReviewRow(label = "Urgency", value = draft.urgency)
                ReviewRow(label = "Facility", value = draft.facilityName.ifBlank { "French Medical Institute for Mothers & Children (FMIC)" })
                ReviewRow(label = "Provider", value = draft.doctorName.ifBlank { "First Available Specialist" })
                ReviewRow(label = "Date & Slot", value = "${draft.appointmentDate} • ${draft.timeSlot}")
                ReviewRow(label = "Format", value = draft.visitType.labelEn)
                ReviewRow(label = "Member ID", value = draft.corporateMemberId)
                ReviewRow(label = "Contact Via", value = draft.contactPreference)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate { it.copy(consentGiven = !it.consentGiven) } }
        ) {
            Checkbox(
                checked = draft.consentGiven,
                onCheckedChange = { chk -> onUpdate { it.copy(consentGiven = chk) } },
                colors = CheckboxDefaults.colors(checkedColor = ClinicalGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "I consent to the confidential processing of my medical appointment information by Yawar Hamdard Health Consulting Services in accordance with healthcare privacy standards.",
                fontSize = 11.sp,
                color = Ink,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Slate, fontSize = 12.sp)
        Text(text = value, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

// Step 9: Confirmation
@Composable
private fun Step9Confirmation(
    referenceId: String,
    draft: com.example.ui.viewmodel.BookingDraft,
    language: AppLanguage,
    onViewAppointments: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PaleGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = ClinicalGreen,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Request Successfully Submitted",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 21.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Unique Booking Reference",
            color = Slate,
            fontSize = 12.sp
        )

        Text(
            text = referenceId,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = YawarNavy,
                fontSize = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status", color = Slate, fontSize = 12.sp)
                    Text("Under Review", color = YawarBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Doctor", color = Slate, fontSize = 12.sp)
                    Text(draft.doctorName.ifBlank { "First Available Specialist" }, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Scheduled Slot", color = Slate, fontSize = 12.sp)
                    Text("${draft.appointmentDate} • ${draft.timeSlot}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Callcenter alert notification
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(PaleBlue)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Alert Sent",
                    tint = YawarNavy,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Operational dispatch logged to callcenter@yawarconsulting.com. You will receive final clinic confirmation shortly.",
                    fontSize = 11.sp,
                    color = YawarNavy,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewAppointments,
            colors = ButtonDefaults.buttonColors(
                containerColor = ClinicalGreen,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View in My Appointments", fontWeight = FontWeight.Bold)
        }
    }
}
