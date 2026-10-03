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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val facilities by viewModel.facilities.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val lastRef by viewModel.lastSubmittedReference.collectAsState()

    // Keep the request simple: account details are shown for context, then the
    // patient can describe the concern in two optional free-text fields.
    val totalSteps = 2

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
                1 -> Step1Who(fullName = profile?.fullName.orEmpty(), phone = profile?.phone.orEmpty())
                2 -> Step2WhatCare(draft = draft, onUpdate = viewModel::updateBookingDraft)
                9 -> Step9Confirmation(
                    referenceId = lastRef ?: "—",
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
    fullName: String,
    phone: String
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
            text = "This care request is linked to the patient account you signed in with.",
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
                            Text(fullName.ifBlank { "Signed-in patient" }, fontWeight = FontWeight.Bold, color = Ink, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = ClinicalGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(phone.ifBlank { "Add a phone number in your account profile" }, fontSize = 12.sp, color = Slate)
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
                            text = "Your request is associated with this signed-in account. Keep your profile details current so the care team can contact you.",
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
    Column {
        Text(
            text = "2. Tell us what you need",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 18.sp
            )
        )
        Text(
            text = "Both fields are optional. A YHCS coordinator will follow up if more detail is needed.",
            color = Slate,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = draft.reasonForCare,
            onValueChange = { reason -> onUpdate { it.copy(reasonForCare = reason) } },
            label = { Text("What type of illness or concern is this? (Optional)") },
            placeholder = { Text("For example: pain, fever, injury, check-up") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = draft.symptomsSummary,
            onValueChange = { sym -> onUpdate { it.copy(symptomsSummary = sym) } },
            label = { Text("Explain the problem (Optional)") },
            placeholder = { Text("Add symptoms, timing, or anything you want the care team to know") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            maxLines = 10
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
    val provinces = listOf("Kabul", "Herat", "Balkh", "Kandahar", "Nangarhar", "Khost")
    Column {
        Text("3. Where are you seeking care?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp))
        Text("Tell the call center your location and preferred visit format. You can choose a hospital after the care team reviews your request.", color = Slate, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text("Visit Format", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisitType.values().forEach { visitType ->
                FilterChip(
                    selected = draft.visitType == visitType,
                    onClick = { onUpdate { it.copy(visitType = visitType) } },
                    label = { Text(visitType.labelEn, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PaleBlue)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("Province", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
        Spacer(modifier = Modifier.height(6.dp))
        provinces.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { province ->
                    FilterChip(
                        selected = draft.province == province,
                        onClick = { onUpdate { it.copy(province = province) } },
                        label = { Text(province, fontSize = 11.sp) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = PaleBlue), shape = MaterialTheme.shapes.small) {
            Text(
                "Hospital referrals are sent after a call-center review. You will see the hospital name and profile before approving the referral.",
                modifier = Modifier.padding(14.dp), color = YawarNavy, fontSize = 13.sp
            )
        }
    }
}

// Step 4: Doctor matching
@Composable
private fun Step4WhoDoctor(
    draft: com.example.ui.viewmodel.BookingDraft
) {
    Column {
        Text("4. Doctor matching", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "A Yawar care coordinator reviews your symptoms and finds an appropriate doctor after speaking with you. This request does not book a doctor directly.",
            color = Slate, fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = PaleGreen), shape = MaterialTheme.shapes.small) {
            Text(
                "Your selected service: ${draft.specialty}\n\nThe call center can message or call you if they need more information.",
                modifier = Modifier.padding(14.dp), color = DeepGreen, fontSize = 13.sp
            )
        }
    }
}
// Step 5: When?
@Composable
private fun Step5When(
    draft: com.example.ui.viewmodel.BookingDraft,
    onUpdate: ((com.example.ui.viewmodel.BookingDraft) -> com.example.ui.viewmodel.BookingDraft) -> Unit
) {
    val dates = (1..4).map { offset ->
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }.time
    }.map { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(it) }
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
            placeholder = { Text("e.g. YH-BIMA-0001") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("Example Bima IDs (demo only): YH-BIMA-0001 · YH-BIMA-0002 · YH-BIMA-0003 · YH-BIMA-0004. These are not verified coverage numbers.", fontSize = 11.sp, color = Slate)

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
                    text = "The request goes to the Yawar call-center queue for review before a hospital referral.",
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
    patientName: String,
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
                ReviewRow(label = "Patient", value = patientName.ifBlank { "Signed-in patient" })
                ReviewRow(label = "Specialty", value = draft.specialty)
                ReviewRow(label = "Urgency", value = draft.urgency)
                ReviewRow(label = "Preferred date and time", value = "${draft.appointmentDate} • ${draft.timeSlot}")
                ReviewRow(label = "Hospital", value = "Chosen after call-center review")
                ReviewRow(label = "Doctor", value = "Assigned after review if needed")
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
                text = "I agree that Yawar may process this care request and share the necessary details with the hospital I approve.",
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
            text = "Care request reference",
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
                    Text("Received", color = YawarBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Next step", color = Slate, fontSize = 12.sp)
                    Text("Call-center review", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Preferred time", color = Slate, fontSize = 12.sp)
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
                    text = "Your request is in the Yawar call-center queue. Follow updates in My care requests or message your care team there.",
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
