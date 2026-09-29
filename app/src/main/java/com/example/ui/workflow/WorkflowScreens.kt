package com.example.ui.workflow

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.HospitalInput
import com.example.data.auth.PaymentInput
import com.example.data.auth.WorkflowDirectMessage
import com.example.data.local.AppointmentEntity
import com.example.data.local.FacilityEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AppointmentStatus
import com.example.ui.common.HospitalLogoBadge
import com.example.ui.common.YhcsContactCard
import com.example.ui.common.YhcsSupportContacts
import com.example.ui.common.chatDoodleWallpaper
import com.example.ui.theme.ChatBubbleIn
import com.example.ui.theme.ChatBubbleOut
import com.example.ui.theme.ChatCanvas
import com.example.ui.theme.ChatMeta
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.YawarViewModel
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientCareRequestsScreen(viewModel: YawarViewModel, onOpenMessages: () -> Unit = {}) {
    val requests by viewModel.appointments.collectAsState()
    val hospitals by viewModel.facilities.collectAsState()
    val refreshing by viewModel.workflowRefreshing.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeading("My care requests", "Your care team reviews each request before referring it to a hospital.", refreshing, viewModel::refreshWorkflow)
        if (requests.isEmpty()) {
            EmptyWorkflowState("No care requests yet", "Start a care request from Home. Your request will appear here after it reaches the Yawar call center.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(requests, key = { it.id }) { request ->
                    PatientRequestCard(request, hospitals, onRoute = { viewModel.routeCareRequest(request.id, it) }, onCall = {
                        viewModel.startCaseVoiceCall(request.id)
                    }, onMessage = {
                        viewModel.selectCareRequest(request.id)
                        onOpenMessages()
                    })
                }
            }
        }
    }
}

@Composable
fun CallCenterDashboardScreen(viewModel: YawarViewModel, onOpenMessages: () -> Unit = {}) {
    val requests by viewModel.appointments.collectAsState()
    val hospitals by viewModel.facilities.collectAsState()
    val doctors by viewModel.managedDoctors.collectAsState()
    val refreshing by viewModel.workflowRefreshing.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeading("Call-center case queue", "Review patient requests, speak with patients, and route approved referrals.", refreshing, viewModel::refreshWorkflow)
        OutlinedButton(onClick = { viewModel.openDirectInbox(); onOpenMessages() }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Icon(Icons.Default.Chat, null); Spacer(Modifier.width(6.dp)); Text("YHCS direct messages")
        }
        QueueMetrics(requests)
        if (requests.isEmpty()) {
            EmptyWorkflowState("The queue is clear", "New patient care requests will appear here after the shared care service is connected.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(requests, key = { it.id }) { request ->
                    StaffRequestCard(
                        request = request,
                        hospitals = hospitals,
                        onReview = { viewModel.reviewCareRequest(request.id) },
                        onRoute = { hospitalId -> viewModel.routeCareRequest(request.id, hospitalId) },
                        doctors = doctors,
                        onAssignDoctor = { doctorId -> viewModel.assignDoctorToRequest(request.id, doctorId) },
                        onCall = { viewModel.startCaseVoiceCall(request.id) },
                        onMessage = {
                            viewModel.selectCareRequest(request.id)
                            onOpenMessages()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HospitalDashboardScreen(
    viewModel: YawarViewModel,
    onOpenMessages: () -> Unit = {},
    onOpenYhcsMessage: (String) -> Unit = {}
) {
    val requests by viewModel.appointments.collectAsState()
    val hospitals by viewModel.facilities.collectAsState()
    val refreshing by viewModel.workflowRefreshing.collectAsState()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeading("Hospital referrals", "Review referrals sent to your hospital, message patients, and upload their records.", refreshing, viewModel::refreshWorkflow)
        YhcsContactCard(onMessage = onOpenYhcsMessage, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        val pendingDocs = requests.count { it.status == AppointmentStatus.AWAITING_PROVIDER && it.attachedDocuments.isEmpty() }
        QueueMetrics(requests, pendingDocs)
        if (requests.isEmpty()) {
            EmptyWorkflowState("No referrals yet", "Cases are shown only after the call center or patient sends a referral to your hospital.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(requests, key = { it.id }) { request ->
                    HospitalRequestCard(
                        request = request,
                        hospitals = hospitals,
                        onMessage = {
                            viewModel.selectCareRequest(request.id)
                            onOpenMessages()
                        },
                        onCall = { viewModel.startCaseVoiceCall(request.id) },
                        onUpload = { uri -> viewModel.uploadHospitalDocument(request.id, context.contentResolver, uri) },
                        onComplete = { viewModel.markHospitalDocumentsComplete(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientRequestCard(
    request: AppointmentEntity,
    hospitals: List<FacilityEntity>,
    onRoute: (String) -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    var menuOpen by remember(request.id) { mutableStateOf(false) }
    var selectedHospital by remember(request.id) { mutableStateOf(request.facilityId) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestSummary(request)
            Text("Status: ${request.status.labelEn}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (request.status == AppointmentStatus.UNDER_REVIEW && request.facilityId.isBlank()) {
                Text("Your care team has reviewed this request. Choose a hospital profile to approve the referral.", fontSize = 13.sp)
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, enabled = hospitals.isNotEmpty()) {
                        Text(hospitals.firstOrNull { it.id == selectedHospital }?.name ?: "Choose a hospital")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        hospitals.forEach { hospital ->
                            DropdownMenuItem(
                                text = { Text(hospital.name) },
                                onClick = { selectedHospital = hospital.id; menuOpen = false }
                            )
                        }
                    }
                }
                Button(onClick = { onRoute(selectedHospital) }, enabled = selectedHospital.isNotBlank()) { Text("Approve and send referral") }
            } else if (request.facilityId.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hospital = hospitals.firstOrNull { it.id == request.facilityId }
                    HospitalLogoBadge(request.facilityName, size = 40.dp, logoUrl = hospital?.logoFile.orEmpty(), hospitalId = request.facilityId)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(request.facilityName, fontWeight = FontWeight.Bold)
                        Text("Referral sent to hospital", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val hospital = hospitals.firstOrNull { it.id == request.facilityId }
                if (hospital != null && hospital.contactPhone.isNotBlank()) {
                    ContactButtons(
                        phone = hospital.contactPhone.substringBefore('/').trim(),
                        whatsappPhone = hospital.whatsappPhone.ifBlank { hospital.contactPhone.substringBefore('/').trim() },
                        message = "Hello, I am following up on referral ${request.id}.",
                        onCall = onCall
                    )
                }
            } else {
                Text("The call center will review your request and contact you.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (request.attachedDocuments.isNotEmpty()) Text("Hospital records: ${request.attachedDocuments.joinToString()}", fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMessage) { Icon(Icons.Default.Chat, null); Spacer(Modifier.width(5.dp)); Text("Message care team") }
                if (request.patientPhone.isNotBlank()) InAppCallButton("In-app call", onCall)
            }
        }
    }
}

@Composable
private fun StaffRequestCard(
    request: AppointmentEntity,
    hospitals: List<FacilityEntity>,
    onReview: () -> Unit,
    onRoute: (String) -> Unit,
    doctors: List<com.example.data.auth.WorkflowDoctor> = emptyList(),
    onAssignDoctor: (String) -> Unit = {},
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    var menuOpen by remember(request.id) { mutableStateOf(false) }
    var doctorMenuOpen by remember(request.id) { mutableStateOf(false) }
    var selectedHospital by remember(request.id) { mutableStateOf(request.facilityId) }
    var selectedDoctor by remember(request.id) { mutableStateOf(request.doctorId) }
    val context = LocalContext.current
    val eligibleDoctors = doctors.filter { it.isVerified && it.hospitalAffiliation == request.facilityName }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestSummary(request)
            Text("Patient phone: ${request.patientPhone.ifBlank { "Not provided" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Status: ${request.status.labelEn}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (request.coordinatorNotes.isNotBlank()) Text(request.coordinatorNotes, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (request.patientPhone.isNotBlank()) {
                    InAppCallButton("Call patient", onCall)
                    OutlinedButton(onClick = { openWhatsApp(context, request.patientPhone, "Hello ${request.patientName}, Yawar call center is following up on request ${request.id}.") }) {
                        Icon(Icons.Default.Chat, null); Text("WhatsApp")
                    }
                }
                OutlinedButton(onClick = onMessage) { Icon(Icons.Default.Chat, null); Text("In-app message") }
            }
            if (request.status == AppointmentStatus.SUBMITTED) Button(onClick = onReview) { Text("Mark reviewed") }
            if (request.status == AppointmentStatus.UNDER_REVIEW || request.status == AppointmentStatus.AWAITING_PROVIDER) {
                Box {
                    OutlinedButton(onClick = { menuOpen = true }, enabled = hospitals.isNotEmpty()) {
                        Text(hospitals.firstOrNull { it.id == selectedHospital }?.name ?: "Choose receiving hospital")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        hospitals.forEach { hospital ->
                            DropdownMenuItem(text = { Text(hospital.name) }, onClick = { selectedHospital = hospital.id; menuOpen = false })
                        }
                    }
                }
                Button(onClick = { onRoute(selectedHospital) }, enabled = selectedHospital.isNotBlank()) {
                    Text(if (request.facilityId.isBlank()) "Send referral" else "Update hospital referral")
                }
            }
            if (request.facilityName.isNotBlank()) Text("Receiving hospital: ${request.facilityName}", fontSize = 12.sp)
            if (request.facilityId.isNotBlank() && eligibleDoctors.isNotEmpty()) {
                Box {
                    OutlinedButton(onClick = { doctorMenuOpen = true }) {
                        Text(eligibleDoctors.firstOrNull { it.id == selectedDoctor }?.name ?: "Assign verified doctor")
                    }
                    DropdownMenu(expanded = doctorMenuOpen, onDismissRequest = { doctorMenuOpen = false }) {
                        eligibleDoctors.forEach { doctor ->
                            DropdownMenuItem(text = { Text(doctor.name) }, onClick = { selectedDoctor = doctor.id; doctorMenuOpen = false })
                        }
                    }
                }
                Button(onClick = { onAssignDoctor(selectedDoctor) }, enabled = selectedDoctor.isNotBlank()) { Text("Assign doctor") }
            }
        }
    }
}

@Composable
private fun HospitalRequestCard(
    request: AppointmentEntity,
    hospitals: List<FacilityEntity>,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onUpload: (Uri) -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(onUpload) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            hospitals.firstOrNull { it.id == request.facilityId }?.let { hospital ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HospitalLogoBadge(hospital.name, size = 40.dp, logoUrl = hospital.logoFile, hospitalId = hospital.id)
                    Text(hospital.name, fontWeight = FontWeight.SemiBold)
                }
            }
            RequestSummary(request)
            Text("Patient phone: ${request.patientPhone.ifBlank { "Not provided" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Status: ${request.status.labelEn}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (request.coordinatorNotes.isNotBlank()) Text(request.coordinatorNotes, fontSize = 12.sp)
            if (request.attachedDocuments.isNotEmpty()) {
                Text("Uploaded patient records", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                request.attachedDocuments.forEach { Text("• $it", fontSize = 12.sp) }
            } else if (request.status == AppointmentStatus.AWAITING_PROVIDER) {
                Text("Patient records are pending. Email reminders need a hospital contact email and configured Hostinger mail.", fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (request.patientPhone.isNotBlank()) {
                    InAppCallButton("Call patient", onCall)
                    OutlinedButton(onClick = { openWhatsApp(context, request.patientPhone, "Hello ${request.patientName}, this is ${request.facilityName} about referral ${request.id}.") }) {
                        Icon(Icons.Default.Chat, null); Text("WhatsApp")
                    }
                }
                OutlinedButton(onClick = onMessage) { Icon(Icons.Default.Chat, null); Text("Message") }
            }
            if (request.status == AppointmentStatus.AWAITING_PROVIDER) {
                OutlinedButton(onClick = { filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }) {
                    Icon(Icons.Default.UploadFile, null); Spacer(Modifier.width(6.dp)); Text("Upload patient record")
                }
                if (request.attachedDocuments.isNotEmpty()) Button(onClick = onComplete) { Text("Mark records complete") }
            }
        }
    }
}

@Composable
fun CareMessagesScreen(viewModel: YawarViewModel) {
    val requests by viewModel.appointments.collectAsState()
    val hospitals by viewModel.facilities.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedId by viewModel.selectedCareRequestId.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedSupportKey by viewModel.selectedSupportKey.collectAsState()
    val selectedDirectId by viewModel.selectedDirectConversationId.collectAsState()
    val directConversations by viewModel.directConversations.collectAsState()
    val directMessages by viewModel.directMessages.collectAsState()
    val directMessagesLoading by viewModel.directMessagesLoading.collectAsState()
    val context = LocalContext.current
    var caseCallPermissionRequested by remember { mutableStateOf(false) }
    val caseCallPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && caseCallPermissionRequested && selectedId.isNotBlank()) viewModel.startCaseVoiceCall(selectedId)
        else if (!granted) viewModel.showSnackbar("Allow microphone access to make an in-app call.")
        caseCallPermissionRequested = false
    }

    LaunchedEffect(selectedId) {
        if (selectedId.isNotBlank()) {
            viewModel.refreshCaseMessages(selectedId)
            while (true) {
                kotlinx.coroutines.delay(5_000)
                viewModel.refreshCaseMessages(selectedId)
            }
        }
    }

    LaunchedEffect(currentRole, selectedId, selectedSupportKey) {
        if (selectedId.isBlank() && selectedSupportKey.isBlank() &&
            currentRole in listOf(com.example.data.model.UserRole.CALL_CENTER, com.example.data.model.UserRole.ADMIN)
        ) viewModel.refreshDirectInbox()
    }
    LaunchedEffect(currentRole, selectedSupportKey, selectedDirectId, selectedId) {
        val isSupportInbox = selectedId.isBlank() && selectedSupportKey.isBlank() &&
            currentRole in listOf(com.example.data.model.UserRole.CALL_CENTER, com.example.data.model.UserRole.ADMIN)
        val isDirectChat = selectedSupportKey.isNotBlank() || selectedDirectId.isNotBlank()
        if (isSupportInbox || isDirectChat) {
            while (true) {
                kotlinx.coroutines.delay(10_000)
                if (isSupportInbox) viewModel.refreshDirectInbox() else viewModel.refreshDirectConversation()
            }
        }
    }
    if (selectedDirectId.isNotBlank() || selectedSupportKey.isNotBlank() || selectedId.isBlank()) {
        YhcsDirectMessagesScreen(
            currentRole = currentRole,
            selectedSupportKey = selectedSupportKey,
            selectedDirectId = selectedDirectId,
            conversations = directConversations,
            requests = requests,
            messages = directMessages,
            loading = directMessagesLoading,
            viewModel = viewModel
        )
        return
    }

    val messageListState = rememberLazyListState()
    val request = requests.firstOrNull { it.id == selectedId }
    val caseMessages = messages.filter { it.conversationId == "case_${request?.id}" }
    LaunchedEffect(caseMessages.size) {
        if (caseMessages.isNotEmpty()) messageListState.animateScrollToItem(caseMessages.lastIndex)
    }
    Column(Modifier.fillMaxSize().background(ChatCanvas).imePadding()) {
        val hospital = request?.let { item -> hospitals.firstOrNull { it.id == item.facilityId } }
        val contactName = when {
            request == null -> "Yawar care team"
            currentRole == com.example.data.model.UserRole.PATIENT -> hospital?.name ?: "Yawar care team"
            else -> request.patientName.ifBlank { "Patient" }
        }
        val contactPhone = if (currentRole == com.example.data.model.UserRole.PATIENT) {
            hospital?.contactPhone.orEmpty()
        } else {
            request?.patientPhone.orEmpty()
        }
        val whatsappPhone = if (currentRole == com.example.data.model.UserRole.PATIENT) {
            hospital?.let { it.whatsappPhone.ifBlank { it.contactPhone } }.orEmpty()
        } else {
            request?.patientPhone.orEmpty()
        }
        Row(
            Modifier.fillMaxWidth().background(WhatsAppGreen).padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::closeCareRequestMessages) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to messages", tint = Color.White)
            }
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(contactName.take(1).uppercase(Locale.getDefault()), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(contactName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(
                    request?.let { "${it.id} · ${it.status.labelEn}" } ?: "Your private care request conversation",
                    color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp, maxLines = 1
                )
            }
            if (contactPhone.isNotBlank()) {
                IconButton(onClick = {
                    if (request != null) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.startCaseVoiceCall(request.id)
                        } else {
                            caseCallPermissionRequested = true
                            caseCallPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }, enabled = request != null) {
                    Icon(Icons.Default.Call, contentDescription = "Call ${contactName} inside the app", tint = Color.White)
                }
            }
            if (whatsappPhone.isNotBlank()) {
                IconButton(onClick = {
                    val ref = request?.id?.let { " about referral $it" }.orEmpty()
                    openWhatsApp(context, whatsappPhone, "Hello $contactName, I am contacting you$ref.")
                }) {
                    Icon(Icons.Default.Chat, contentDescription = "Open WhatsApp with ${contactName}", tint = Color.White)
                }
            }
        }
        if (requests.isEmpty()) {
            EmptyWorkflowState("No care conversations", "A message thread opens when a patient care request is created.")
            return@Column
        }
        LazyRow(
            Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
        ) {
            items(requests, key = { "thread_${it.id}" }) { item ->
                FilterChip(
                    selected = item.id == selectedId,
                    onClick = { viewModel.selectCareRequest(item.id) },
                    label = { Text("${item.patientName} · ${item.id}", maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = com.example.ui.theme.WhatsAppPaleGreen,
                        selectedLabelColor = WhatsAppGreen
                    )
                )
            }
        }
        Box(
            Modifier.weight(1f).fillMaxWidth().background(ChatCanvas).chatDoodleWallpaper(WhatsAppGreen.copy(alpha = 0.55f))
        ) {
            if (caseMessages.isEmpty()) {
                Text(
                    "Messages about this request will appear here.",
                    Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(caseMessages, key = { it.id }) { message ->
                        CaseWorkflowMessageBubble(
                            message = message,
                            outgoing = message.senderRole.equals(currentRole.name, ignoreCase = true),
                            onDownload = { onReady -> viewModel.downloadCaseAttachment(request?.id.orEmpty(), message, onReady) },
                            onOpenFile = { file, mime -> openDirectMedia(context, file, mime) }
                        )
                    }
                }
            }
        }
        key("case_composer_${request?.id.orEmpty()}") {
            DirectChatComposer(
                viewModel = viewModel,
                enabled = request != null,
                onSendText = { body, complete -> request?.let { viewModel.sendMessage(body, "case_${it.id}", onComplete = complete) } ?: complete(false) },
                onSendMedia = { media, caption, complete -> request?.let { viewModel.sendCaseAttachment(it.id, media.file, media.mimeType, media.fileName, caption, media.durationSeconds, complete) } ?: complete(false) }
            )
        }
    }
}

@Composable
private fun YhcsDirectMessagesScreen(
    currentRole: com.example.data.model.UserRole,
    selectedSupportKey: String,
    selectedDirectId: String,
    conversations: List<com.example.data.auth.WorkflowDirectConversation>,
    requests: List<com.example.data.local.AppointmentEntity>,
    messages: List<com.example.data.auth.WorkflowDirectMessage>,
    loading: Boolean,
    viewModel: YawarViewModel
) {
    val isStaffAccount = currentRole in listOf(com.example.data.model.UserRole.CALL_CENTER, com.example.data.model.UserRole.ADMIN)
    val isInbox = selectedSupportKey.isBlank() && selectedDirectId.isBlank()
    val selectedConversation = conversations.firstOrNull { it.id == selectedDirectId }
    val selectedSupport = YhcsSupportContacts.firstOrNull { it.key == selectedSupportKey }
    val title = when {
        selectedSupport != null -> selectedSupport.label
        selectedConversation != null -> if (isStaffAccount) {
            selectedConversation.participantName.ifBlank { "App user" }
        } else {
            selectedConversation.supportName.ifBlank { selectedConversation.supportKey.replace("YHCS", "YHCS ") }
        }
        else -> "YHCS direct messages"
    }
    val phone = selectedSupport?.phone ?: if (isStaffAccount) {
        selectedConversation?.participantPhone.orEmpty()
    } else {
        YhcsSupportContacts.firstOrNull { it.key == selectedConversation?.supportKey }?.phone.orEmpty()
    }
    val activeVoiceCall by viewModel.voiceCall.collectAsState()
    val messageListState = rememberLazyListState()
    val context = LocalContext.current
    var requestingCallPermission by remember { mutableStateOf(false) }
    val callPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && requestingCallPermission) viewModel.startDirectVoiceCall()
        else if (!granted) viewModel.showSnackbar("Allow microphone access to make an in-app call.")
        requestingCallPermission = false
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) messageListState.animateScrollToItem(messages.lastIndex)
    }

    if (isInbox) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(
                Modifier.fillMaxWidth().background(WhatsAppGreen).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(if (isStaffAccount) "YHCS direct messages" else "Messages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        if (isStaffAccount) "Private conversations assigned to your account" else "YHCS desks and care request conversations",
                        color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp
                    )
                }
                IconButton(onClick = viewModel::refreshDirectInbox) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isStaffAccount) item(key = "yhcs_contacts") {
                    YhcsContactCard(onMessage = viewModel::openYhcsConversation)
                }
                if (conversations.isEmpty() && requests.isEmpty()) item(key = "empty_messages") {
                    EmptyWorkflowState("No conversations yet", "Choose a YHCS desk or create a care request to start a private conversation.")
                }
                items(conversations, key = { "direct_${it.id}" }) { conversation ->
                    Card(
                        Modifier.fillMaxWidth().clickable { viewModel.selectDirectConversation(conversation.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    if (isStaffAccount) conversation.participantName.ifBlank { "App user" }
                                    else conversation.supportName.ifBlank { conversation.supportKey.replace("YHCS", "YHCS ") },
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(conversation.supportKey.replace("YHCS", "YHCS "), color = WhatsAppGreen, fontSize = 12.sp)
                                    if (conversation.unreadCount > 0) Surface(shape = CircleShape, color = WhatsAppGreen) {
                                        Text(conversation.unreadCount.coerceAtMost(99).toString(), color = Color.White, fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                    }
                                }
                            }
                            val displayPhone = if (isStaffAccount) conversation.participantPhone else ""
                            if (displayPhone.isNotBlank()) Text(displayPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(conversation.lastMessage.ifBlank { "Start the conversation" }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                }
                if (requests.isNotEmpty()) item(key = "care_request_heading") {
                    Text("Care request conversations", Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                items(requests, key = { "case_${it.id}" }) { request ->
                    Card(
                        Modifier.fillMaxWidth().clickable { viewModel.selectCareRequest(request.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(request.patientName.ifBlank { request.facilityName.ifBlank { "Care team" } }, fontWeight = FontWeight.Bold)
                            Text("${request.id} · ${request.status.labelEn}", fontSize = 12.sp, color = WhatsAppGreen)
                            if (request.facilityName.isNotBlank()) Text(request.facilityName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize().background(ChatCanvas).imePadding()) {
        Row(
            Modifier.fillMaxWidth().background(WhatsAppGreen).padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedDirectId.isNotBlank() || selectedSupportKey.isNotBlank()) {
                IconButton(onClick = viewModel::closeDirectConversation) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back to inbox", tint = Color.White)
                }
            }
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(title.take(1).uppercase(Locale.getDefault()), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(
                    when {
                        selectedSupport != null -> "Private message to ${selectedSupport.label} · ${selectedSupport.phone}"
                        selectedConversation != null -> selectedConversation.supportKey.replace("YHCS", "YHCS ") + " · " + (phone.ifBlank { "Private conversation" })
                        else -> "Private conversation"
                    },
                    color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp, maxLines = 1
                )
            }
            if (phone.isNotBlank()) IconButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.startDirectVoiceCall()
                    } else {
                        requestingCallPermission = true
                        callPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = selectedDirectId.isNotBlank() && activeVoiceCall == null
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call $title in the app", tint = Color.White)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(ChatCanvas).chatDoodleWallpaper(WhatsAppGreen.copy(alpha = 0.55f))) {
            if (messages.isEmpty()) {
                Text(
                    if (loading) "Opening conversation…" else "Send a message to start this conversation.",
                    Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        DirectWorkflowMessageBubble(
                            message = message,
                            outgoing = message.senderRole.equals(currentRole.name, ignoreCase = true),
                            onDownload = { onReady -> viewModel.downloadDirectAttachment(message, onReady) },
                            onOpenFile = { file, mime -> openDirectMedia(context, file, mime) }
                        )
                    }
                }
            }
        }
        key("direct_composer_$selectedDirectId") {
            DirectChatComposer(
                viewModel = viewModel,
                enabled = selectedDirectId.isNotBlank() && !loading,
                onSendText = { body, complete -> viewModel.sendDirectMessage(body, complete) },
                onSendMedia = { media, caption, complete ->
                    viewModel.sendDirectAttachment(media.file, media.mimeType, media.fileName, caption, media.durationSeconds, complete)
                }
            )
        }
    }
}

@Composable
fun VoiceCallMonitor(viewModel: YawarViewModel) {
    val context = LocalContext.current
    val incomingCalls by viewModel.incomingVoiceCalls.collectAsState()
    val activeVoiceCall by viewModel.voiceCall.collectAsState()
    var requestedAnswerCallId by remember { mutableStateOf("") }
    val callPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val incoming = incomingCalls.firstOrNull { it.id == requestedAnswerCallId }
        if (granted && incoming != null) viewModel.acceptIncomingVoiceCall(incoming)
        else if (!granted) viewModel.showSnackbar("Allow microphone access to answer an in-app call.")
        requestedAnswerCallId = ""
    }
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshIncomingVoiceCalls()
            kotlinx.coroutines.delay(2_500)
        }
    }
    val incoming = incomingCalls.firstOrNull().takeIf { activeVoiceCall == null }
    if (incoming != null) Dialog(
        onDismissRequest = { viewModel.rejectIncomingVoiceCall(incoming) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF2F8F4)) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("INCOMING IN-APP CALL", color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CallPeerAvatar(incoming.peerName, incoming.peerPhotoUrl, 164.dp)
                    Text(incoming.peerName, fontWeight = FontWeight.Bold, fontSize = 27.sp, color = Color(0xFF17352A))
                    Text("Calling you over the internet", color = Color(0xFF61756C), fontSize = 15.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    CallAction(
                        label = "Decline", icon = Icons.Default.CallEnd, tint = Color.White,
                        background = Color(0xFFD94343), onClick = { viewModel.rejectIncomingVoiceCall(incoming) }
                    )
                    CallAction(
                        label = "Answer", icon = Icons.Default.Call, tint = Color.White,
                        background = WhatsAppGreen, onClick = {
                            requestedAnswerCallId = incoming.id
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.acceptIncomingVoiceCall(incoming)
                            } else callPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceCallScreen(viewModel: YawarViewModel, call: com.example.ui.viewmodel.VoiceCallUiState, onMinimize: () -> Unit) {
    BackHandler(enabled = true, onBack = onMinimize)
    var now by remember(call.callId) { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(call.callId) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    val elapsedSeconds = if (call.connectedAtTimestamp > 0L) ((now - call.connectedAtTimestamp) / 1000).coerceAtLeast(0) else 0
    val elapsedLabel = "%02d:%02d".format(Locale.US, elapsedSeconds / 60, elapsedSeconds % 60)
    val status = when {
        call.status == "Connected" -> elapsedLabel
        call.status.startsWith("Call could not") -> call.status
        call.status == "Connection interrupted" -> "Reconnecting…"
        else -> call.status
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF2F8F4)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinimize) { Icon(Icons.Default.ArrowBack, contentDescription = "Minimize call", tint = WhatsAppGreen) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("YAWAR INTERNET CALL", color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.1.sp)
                    Text("Private voice call", color = Color(0xFF789087), fontSize = 13.sp)
                }
                Spacer(Modifier.size(48.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CallPeerAvatar(call.peerName, call.peerPhotoUrl, 184.dp)
                Text(call.peerName, color = Color(0xFF17352A), fontWeight = FontWeight.Bold, fontSize = 28.sp, maxLines = 2)
                Text(status, color = if (call.status.startsWith("Call could not")) MaterialTheme.colorScheme.error else WhatsAppGreen, fontSize = 16.sp)
                if (call.status == "Connected") Text("Connected over the internet", color = Color(0xFF789087), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    CallAction(
                        label = if (call.muted) "Unmute" else "Mute",
                        icon = if (call.muted) Icons.Default.MicOff else Icons.Default.Mic,
                        tint = if (call.muted) Color.White else Color(0xFF2D493D),
                        background = if (call.muted) WhatsAppGreen else Color.White,
                        onClick = viewModel::toggleVoiceCallMute
                    )
                    CallAction(
                        label = if (call.speakerOn) "Earpiece" else "Speaker",
                        icon = Icons.Default.VolumeUp,
                        tint = if (call.speakerOn) Color.White else Color(0xFF2D493D),
                        background = if (call.speakerOn) WhatsAppGreen else Color.White,
                        onClick = viewModel::toggleVoiceCallSpeaker
                    )
                }
                CallAction(
                    label = "End call", icon = Icons.Default.CallEnd,
                    tint = Color.White, background = Color(0xFFD94343), onClick = viewModel::endVoiceCall,
                    buttonSize = 72.dp
                )
                Text("Calls use your mobile data or Wi-Fi connection.", color = Color(0xFF789087), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CallPeerAvatar(name: String, photoUrl: String, size: androidx.compose.ui.unit.Dp) {
    val initials = if (name.trim().startsWith("YHCS", ignoreCase = true)) "YH" else {
        name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase(Locale.getDefault()) }.ifBlank { "YH" }
    }
    Surface(shape = CircleShape, color = Color(0xFFDCEFE5), modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.28f).sp)
            if (photoUrl.startsWith("https://", ignoreCase = true)) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "$name profile photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CallAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = 62.dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = CircleShape, color = background, shadowElevation = if (background == Color.White) 2.dp else 0.dp,
            modifier = Modifier.size(buttonSize).clickable(onClick = onClick)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(27.dp)) }
        }
        Text(label, color = Color(0xFF2D493D), fontSize = 12.sp)
    }
}

private data class PendingDirectMedia(val file: File, val mimeType: String, val durationSeconds: Int = 0, val fileName: String = file.name)

@Composable
private fun DirectChatComposer(
    viewModel: YawarViewModel,
    enabled: Boolean,
    onSendText: (String, (Boolean) -> Unit) -> Unit,
    onSendMedia: (PendingDirectMedia, String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var pendingMedia by remember { mutableStateOf<PendingDirectMedia?>(null) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var recordingStartedAt by remember { mutableStateOf(0L) }
    var sending by remember { mutableStateOf(false) }
    val currentRecorder by androidx.compose.runtime.rememberUpdatedState(recorder)
    val currentRecordingFile by androidx.compose.runtime.rememberUpdatedState(recordingFile)
    val currentPendingMedia by androidx.compose.runtime.rememberUpdatedState(pendingMedia)
    val currentSending by androidx.compose.runtime.rememberUpdatedState(sending)

    fun beginRecording() {
        runCatching {
            val directory = File(context.cacheDir, "direct-media").apply { mkdirs() }
            val output = File(directory, "voice_${System.currentTimeMillis()}.m4a")
            @Suppress("DEPRECATION")
            val mediaRecorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(output.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            recordingFile = output
            recordingStartedAt = System.currentTimeMillis()
        }.onFailure { viewModel.showSnackbar("Could not start voice recording: ${it.message ?: "microphone unavailable"}") }
    }

    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) beginRecording() else viewModel.showSnackbar("Allow microphone access to record a voice message.")
    }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            var copiedFile: File? = null
            runCatching {
                val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }?.substringAfterLast('/')?.substringAfterLast('\\')?.take(120).orEmpty().ifBlank { "attachment" }
                val rawMime = context.contentResolver.getType(uri)?.lowercase(Locale.ROOT).orEmpty()
                val mime = when {
                    rawMime == "image/jpg" -> "image/jpeg"
                    rawMime.isNotBlank() && rawMime != "application/octet-stream" -> rawMime
                    displayName.endsWith(".jpg", true) || displayName.endsWith(".jpeg", true) -> "image/jpeg"
                    displayName.endsWith(".png", true) -> "image/png"
                    displayName.endsWith(".webp", true) -> "image/webp"
                    displayName.endsWith(".pdf", true) -> "application/pdf"
                    else -> rawMime.ifBlank { "application/octet-stream" }
                }
                val extension = when (mime) {
                    "image/jpeg" -> ".jpg"
                    "image/png" -> ".png"
                    "image/webp" -> ".webp"
                    "application/pdf" -> ".pdf"
                    else -> throw IllegalArgumentException("Choose a supported photo or PDF document.")
                }
                val directory = File(context.cacheDir, "direct-media").apply { mkdirs() }
                val output = File(directory, "attachment_${System.currentTimeMillis()}$extension")
                copiedFile = output
                context.contentResolver.openInputStream(uri)?.use { input ->
                    output.outputStream().use { outputStream ->
                        val buffer = ByteArray(16 * 1024)
                        var total = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            total += read
                            if (total > 8L * 1024 * 1024) throw IllegalArgumentException("Attachments must be smaller than 8 MB.")
                            outputStream.write(buffer, 0, read)
                        }
                    }
                } ?: throw IllegalArgumentException("The selected file could not be read.")
                if (output.length() == 0L) throw IllegalArgumentException("The selected file is empty.")
                pendingMedia = PendingDirectMedia(output, mime, fileName = displayName)
            }.onFailure {
                copiedFile?.delete()
                viewModel.showSnackbar(it.message ?: "Could not open that file.")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentRecorder?.let { active -> runCatching { active.stop() }; runCatching { active.release() } }
            currentRecordingFile?.delete()
            if (!currentSending) currentPendingMedia?.file?.delete()
        }
    }
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            pendingMedia?.let { media ->
                Row(Modifier.fillMaxWidth().padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (sending) "Sending ${media.fileName}…" else "${media.fileName} · ${(media.file.length() / 1024).coerceAtLeast(1)} KB · tap × to remove",
                        Modifier.weight(1f), color = WhatsAppGreen, fontSize = 12.sp, maxLines = 1
                    )
                    if (sending) CircularProgressIndicator(Modifier.size(17.dp), color = WhatsAppGreen, strokeWidth = 2.dp)
                    else TextButton(onClick = { media.file.delete(); pendingMedia = null }, enabled = enabled) { Text("×", color = WhatsAppGreen) }
                }
            }
            if (sending && pendingMedia == null) {
                Row(Modifier.padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = WhatsAppGreen, strokeWidth = 2.dp)
                    Text("Sending message…", color = WhatsAppGreen, fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { attachmentPicker.launch(arrayOf("image/jpeg", "image/png", "image/webp", "application/pdf")) }, enabled = enabled && recorder == null && !sending) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach a photo or PDF", tint = WhatsAppGreen)
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    enabled = enabled && recorder == null && !sending
                )
                IconButton(
                    onClick = {
                        if (recorder != null) {
                            val seconds = ((System.currentTimeMillis() - recordingStartedAt) / 1000).toInt().coerceIn(1, 600)
                            val active = recorder
                            val output = recordingFile
                            recorder = null
                            recordingFile = null
                            runCatching { active?.stop() }.onFailure {
                                viewModel.showSnackbar("The voice message was too short to save.")
                                return@IconButton
                            }
                            runCatching { active?.release() }
                            if (output != null && output.isFile && output.length() > 0L) {
                                pendingMedia = PendingDirectMedia(output, "audio/mp4", seconds)
                            } else {
                                viewModel.showSnackbar("The voice message could not be saved.")
                            }
                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            beginRecording()
                        } else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    enabled = enabled && !sending
                ) {
                    Icon(if (recorder == null) Icons.Default.Mic else Icons.Default.Stop,
                        contentDescription = if (recorder == null) "Record voice message" else "Stop recording",
                        tint = if (recorder == null) WhatsAppGreen else MaterialTheme.colorScheme.error)
                }
                val canSend = enabled && !sending && (messageText.isNotBlank() || pendingMedia != null)
                IconButton(
                    onClick = {
                        val media = pendingMedia
                        sending = true
                        val complete: (Boolean) -> Unit = { success ->
                            sending = false
                            if (success) {
                                messageText = ""
                                pendingMedia?.file?.delete()
                                pendingMedia = null
                            }
                        }
                        if (media != null) onSendMedia(media, messageText, complete)
                        else onSendText(messageText, complete)
                    },
                    enabled = canSend,
                    modifier = Modifier.size(46.dp).clip(CircleShape)
                        .background(if (canSend) WhatsAppGreen else WhatsAppGreen.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send message", tint = Color.White, modifier = Modifier.size(21.dp))
                }
            }
        }
    }
}

@Composable
private fun DirectWorkflowMessageBubble(
    message: WorkflowDirectMessage,
    outgoing: Boolean,
    onDownload: (((File, String) -> Unit) -> Unit),
    onOpenFile: (File, String) -> Unit
) {
    var localFile by remember(message.id) { mutableStateOf<File?>(null) }
    val shownText = when {
        message.content.isNotBlank() -> message.content
        message.messageType == "VOICE" -> "Voice message"
        message.messageType == "ATTACHMENT" -> message.attachmentName.ifBlank { "Attachment" }
        else -> ""
    }
    Column(horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start) {
        MessageBubble(
            MessageEntity(
                id = message.id, senderRole = message.senderRole, senderName = message.senderName,
                content = shownText, timestamp = message.timestamp, conversationId = "direct", deliveryStatus = "SENT"
            ),
            outgoing = outgoing
        )
        if (message.messageType == "VOICE" || message.messageType == "ATTACHMENT") {
            if (message.messageType == "VOICE" && localFile != null) {
                VoiceNotePlayer(localFile!!, message.voiceDurationSec)
            } else if (message.messageType == "ATTACHMENT" && message.attachmentMimeType.startsWith("image/") && localFile != null) {
                AsyncImage(
                    model = localFile,
                    contentDescription = message.attachmentName.ifBlank { "Photo attachment" },
                    modifier = Modifier.width(240.dp).height(180.dp).clip(RoundedCornerShape(14.dp)).clickable { onOpenFile(localFile!!, message.attachmentMimeType) },
                    contentScale = ContentScale.Crop
                )
            } else {
                TextButton(onClick = {
                    onDownload { file, mime ->
                        localFile = file
                        if (message.messageType == "ATTACHMENT" && !mime.startsWith("image/")) onOpenFile(file, mime)
                    }
                }) {
                    Icon(Icons.Default.UploadFile, null)
                    Text(when {
                        message.messageType == "VOICE" -> "Download voice message"
                        message.attachmentMimeType.startsWith("image/") -> "Load photo preview"
                        else -> "Open ${message.attachmentName.ifBlank { "attachment" }}"
                    })
                }
            }
        }
    }
}

@Composable
private fun CaseWorkflowMessageBubble(
    message: MessageEntity,
    outgoing: Boolean,
    onDownload: (((File, String) -> Unit) -> Unit),
    onOpenFile: (File, String) -> Unit
) {
    var localFile by remember(message.id) { mutableStateOf<File?>(null) }
    val displayText = when {
        message.content.isNotBlank() -> message.content
        message.messageType == "VOICE" -> "Voice message"
        message.messageType == "ATTACHMENT" -> message.attachmentName.ifBlank { "Attachment" }
        else -> ""
    }
    Column(horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start) {
        MessageBubble(message.copy(content = displayText), outgoing)
        if (message.messageType == "VOICE" || message.messageType == "ATTACHMENT") {
            if (message.messageType == "VOICE" && localFile != null) VoiceNotePlayer(localFile!!, message.voiceDurationSec)
            else if (message.messageType == "ATTACHMENT" && message.attachmentMimeType.startsWith("image/") && localFile != null) {
                AsyncImage(
                    model = localFile,
                    contentDescription = message.attachmentName.ifBlank { "Photo attachment" },
                    modifier = Modifier.width(240.dp).height(180.dp).clip(RoundedCornerShape(14.dp)).clickable { onOpenFile(localFile!!, message.attachmentMimeType) },
                    contentScale = ContentScale.Crop
                )
            }
            else TextButton(onClick = {
                onDownload { file, mime ->
                    localFile = file
                    if (message.messageType == "ATTACHMENT" && !mime.startsWith("image/")) onOpenFile(file, mime)
                }
            }) {
                Icon(Icons.Default.UploadFile, null)
                Text(when {
                    message.messageType == "VOICE" -> "Download voice message"
                    message.attachmentMimeType.startsWith("image/") -> "Load photo preview"
                    else -> "Open ${message.attachmentName.ifBlank { "attachment" }}"
                })
            }
        }
    }
}

@Composable
private fun VoiceNotePlayer(file: File, durationSeconds: Int) {
    var playing by remember(file) { mutableStateOf(false) }
    val player = remember(file) { MediaPlayer() }
    DisposableEffect(player) {
        onDispose { runCatching { player.stop() }; player.release() }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            if (playing) {
                runCatching { player.pause() }
                playing = false
            } else {
                runCatching {
                    player.reset()
                    player.setDataSource(file.absolutePath)
                    player.setOnPreparedListener { it.start(); playing = true }
                    player.setOnCompletionListener { playing = false }
                    player.prepareAsync()
                }
            }
        }) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (playing) "Pause" else "Play voice message", tint = WhatsAppGreen)
        }
        Text("Voice message · ${durationSeconds}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun openDirectMedia(context: Context, file: File, mimeType: String) {
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mimeType).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Open attachment"))
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, outgoing: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(
            Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (outgoing) 14.dp else 3.dp,
                bottomEnd = if (outgoing) 3.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(containerColor = if (outgoing) ChatBubbleOut else ChatBubbleIn),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(start = 11.dp, top = 8.dp, end = 11.dp, bottom = 6.dp)) {
                if (!outgoing) Text(message.senderName, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = WhatsAppGreen)
                Text(message.content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)), fontSize = 10.sp, color = ChatMeta)
                    if (outgoing) Text("✓", color = WhatsAppGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HospitalPayoutsScreen(viewModel: YawarViewModel, canEdit: Boolean) {
    val hospitals by viewModel.facilities.collectAsState()
    val payments by viewModel.hospitalPayments.collectAsState()
    var selectedHospital by remember { mutableStateOf("") }
    var month by remember { mutableStateOf(SimpleDateFormat("yyyy-MM", Locale.US).format(Date())) }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("SCHEDULED") }
    var menuOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeading("Hospital payouts", "Track each hospital's monthly payout, amount, due date, and payment status.", false, viewModel::refreshWorkflow)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (canEdit) item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Schedule or record a payout", fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(onClick = { menuOpen = true }) { Text(hospitals.firstOrNull { it.id == selectedHospital }?.name ?: "Choose hospital") }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                hospitals.forEach { hospital -> DropdownMenuItem(text = { Text(hospital.name) }, onClick = { selectedHospital = hospital.id; menuOpen = false }) }
                            }
                        }
                        OutlinedTextField(month, { month = it }, label = { Text("Month (YYYY-MM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(amount, { amount = it }, label = { Text("Amount (AFN)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Due date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selectedStatus == "SCHEDULED", { selectedStatus = "SCHEDULED" }, label = { Text("Scheduled") })
                            FilterChip(selectedStatus == "PAID", { selectedStatus = "PAID" }, label = { Text("Paid") })
                        }
                        Button(onClick = {
                            val hospitalId = selectedHospital.toLongOrNull()
                            val value = amount.toDoubleOrNull()
                            if (hospitalId == null || value == null || dueDate.isBlank()) {
                                viewModel.showSnackbar("Choose a hospital and enter a valid amount and due date.")
                            } else {
                                viewModel.saveHospitalPayout(PaymentInput(hospitalId, month.trim(), value, dueDate.trim(), selectedStatus, notes.trim()))
                            }
                        }) { Text("Save monthly payout") }
                    }
                }
            }
            if (payments.isEmpty()) item { EmptyWorkflowState("No payout records", "Add a hospital payout with its agreed amount and due date. No amounts are invented by the app.") }
            items(payments, key = { it.id }) { payment ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(payment.hospitalName, fontWeight = FontWeight.Bold)
                        Text("${payment.month.take(7)} · ${payment.amountAf} AFN", fontSize = 14.sp)
                        Text("Due ${payment.dueDate} · ${if (payment.status == "PAID") "Paid" else "Scheduled"}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        if (payment.paidAt.isNotBlank()) Text("Paid at ${payment.paidAt}", fontSize = 12.sp)
                        if (payment.notes.isNotBlank()) Text(payment.notes, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerDashboardScreen(viewModel: YawarViewModel, onOpenMessages: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val requests by viewModel.appointments.collectAsState()
    val doctors by viewModel.managedDoctors.collectAsState()
    val hospitals by viewModel.facilities.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("Yawar manager", Modifier.padding(start = 16.dp, top = 12.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Manage the care queue, hospitals, doctor accounts, and approvals.", Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TabRow(selectedTabIndex = selectedTab) {
            listOf("Operations", "Hospitals", "Doctors").forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
            }
        }
        when (selectedTab) {
            0 -> {
                OutlinedButton(onClick = { viewModel.openDirectInbox(); onOpenMessages() }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Chat, null); Spacer(Modifier.width(6.dp)); Text("YHCS direct messages")
                }
                QueueMetrics(requests)
                if (requests.isEmpty()) EmptyWorkflowState("No open cases", "The manager can review every request in the shared call-center queue.")
                else LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(requests, key = { it.id }) { request ->
                        StaffRequestCard(
                            request = request,
                            hospitals = hospitals,
                            onReview = { viewModel.reviewCareRequest(request.id) },
                            onRoute = { viewModel.routeCareRequest(request.id, it) },
                            doctors = doctors,
                            onAssignDoctor = { viewModel.assignDoctorToRequest(request.id, it) },
                            onCall = { viewModel.startCaseVoiceCall(request.id) },
                            onMessage = { viewModel.selectCareRequest(request.id); onOpenMessages() }
                        )
                    }
                }
            }
            1 -> HospitalManagement(viewModel, hospitals)
            else -> DoctorManagement(viewModel, doctors, hospitals)
        }
    }
}

@Composable
private fun HospitalManagement(viewModel: YawarViewModel, hospitals: List<FacilityEntity>) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var accountHospital by remember { mutableStateOf("") }
    var accountEmail by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var accountPhone by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add hospital", fontWeight = FontWeight.Bold)
                    FormField("Hospital name", name) { name = it }
                    FormField("Contact email", email) { email = it }
                    FormField("Phone", phone) { phone = it }
                    FormField("WhatsApp number", whatsapp) { whatsapp = it }
                    FormField("Province", province) { province = it }
                    FormField("City", city) { city = it }
                    FormField("Address", address) { address = it }
                    FormField("Public logo URL (optional)", logoUrl) { logoUrl = it }
                    Button(onClick = {
                        if (name.isBlank()) viewModel.showSnackbar("Enter the hospital name.")
                        else {
                            viewModel.addHospital(HospitalInput(name.trim(), email.trim(), phone.trim(), whatsapp.trim(), province.trim(), city.trim(), address.trim(), logoUrl.trim()))
                            name = ""; email = ""; phone = ""; whatsapp = ""; province = ""; city = ""; address = ""; logoUrl = ""
                        }
                    }) { Text("Add hospital") }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Create hospital login", fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { menu = true }) { Text(hospitals.firstOrNull { it.id == accountHospital }?.name ?: "Choose hospital") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            hospitals.forEach { hospital -> DropdownMenuItem(text = { Text(hospital.name) }, onClick = { accountHospital = hospital.id; menu = false }) }
                        }
                    }
                    FormField("Staff full name", accountName) { accountName = it }
                    FormField("Staff email", accountEmail) { accountEmail = it }
                    FormField("Phone", accountPhone) { accountPhone = it }
                    Button(onClick = {
                        if (accountHospital.isBlank() || accountName.isBlank() || accountEmail.isBlank()) viewModel.showSnackbar("Choose the hospital and enter the staff member's name and email.")
                        else viewModel.addHospitalAccount(accountHospital, accountEmail, accountName, accountPhone)
                    }) { Text("Create hospital account") }
                    Text("The staff member sets a password through Forgot password.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Hospital directory", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
        items(hospitals, key = { it.id }) { hospital ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    HospitalLogoBadge(hospital.name, size = 42.dp, logoUrl = hospital.logoFile, hospitalId = hospital.id)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(hospital.name, fontWeight = FontWeight.SemiBold)
                        Text("${hospital.district}, ${hospital.province} · ${hospital.contactPhone}", fontSize = 11.sp)
                    }
                    if (hospital.id.toLongOrNull() != null) TextButton(onClick = { viewModel.removeHospital(hospital.id) }) { Text("Remove") }
                }
            }
        }
    }
}

@Composable
private fun DoctorManagement(viewModel: YawarViewModel, doctors: List<com.example.data.auth.WorkflowDoctor>, hospitals: List<FacilityEntity>) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var hospitalId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add doctor account", fontWeight = FontWeight.Bold)
                    FormField("Doctor's full name", fullName) { fullName = it }
                    FormField("Email", email) { email = it }
                    FormField("Phone", phone) { phone = it }
                    FormField("Specialty", specialty) { specialty = it }
                    FormField("Medical license number", license) { license = it }
                    FormField("Doctor photo URL (optional)", photoUrl) { photoUrl = it }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(hospitals.firstOrNull { it.id == hospitalId }?.name ?: "Affiliated hospital (optional)") }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            hospitals.forEach { hospital -> DropdownMenuItem(text = { Text(hospital.name) }, onClick = { hospitalId = hospital.id; expanded = false }) }
                        }
                    }
                    Button(onClick = {
                        if (fullName.isBlank() || email.isBlank() || specialty.isBlank() || license.isBlank()) viewModel.showSnackbar("Enter the doctor's name, email, specialty and license number.")
                        else {
                            viewModel.addDoctor(email, fullName, phone, specialty, license, hospitalId, photoUrl)
                            fullName = ""; email = ""; phone = ""; specialty = ""; license = ""; hospitalId = ""; photoUrl = ""
                        }
                    }) { Text("Create doctor account") }
                    Text("New doctor accounts start unverified. Verify credentials before they receive referrals.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Doctor accounts", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
        if (doctors.isEmpty()) item { Text("No manager-created doctor accounts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(doctors, key = { it.id }) { doctor ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(doctor.name, fontWeight = FontWeight.Bold)
                    Text("${doctor.specialty} · ${doctor.hospitalAffiliation}", fontSize = 12.sp)
                    Text("License: ${doctor.licenseNo} · ${doctor.verificationStatus}", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!doctor.isVerified) Button(onClick = { viewModel.setDoctorVerified(doctor.id, true) }) { Text("Verify") }
                        else OutlinedButton(onClick = { viewModel.setDoctorVerified(doctor.id, false) }) { Text("Revoke verification") }
                        TextButton(onClick = { viewModel.removeDoctor(doctor.id) }) { Text("Deactivate") }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueMetrics(requests: List<AppointmentEntity>, extraPending: Int = 0) {
    val waiting = requests.count { it.status == AppointmentStatus.SUBMITTED || it.status == AppointmentStatus.UNDER_REVIEW }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard("Waiting", waiting.toString(), Modifier.weight(1f))
        MetricCard("At hospital", requests.count { it.status == AppointmentStatus.AWAITING_PROVIDER }.toString(), Modifier.weight(1f))
        MetricCard("Records pending", extraPending.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScreenHeading(title: String, subtitle: String, refreshing: Boolean, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRefresh, enabled = !refreshing) { Icon(Icons.Default.Refresh, contentDescription = "Refresh shared records") }
    }
}

@Composable
private fun RequestSummary(request: AppointmentEntity) {
    Text(request.patientName.ifBlank { "Patient" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Text("${request.id} · ${request.specialty} · ${request.urgency}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
    if (request.reasonForCare.isNotBlank()) Text(request.reasonForCare, fontSize = 14.sp)
    if (request.symptomsSummary.isNotBlank()) Text(request.symptomsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text("${request.province} · ${request.visitType.labelEn}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun EmptyWorkflowState(title: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun ContactButtons(phone: String, whatsappPhone: String, message: String, onCall: () -> Unit) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InAppCallButton("In-app call", onCall)
        OutlinedButton(onClick = { openWhatsApp(context, whatsappPhone, message) }) { Icon(Icons.Default.Chat, null); Text("WhatsApp") }
    }
}

@Composable
private fun InAppCallButton(label: String, onCall: () -> Unit) {
    val context = LocalContext.current
    var waitingForPermission by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && waitingForPermission) onCall()
        waitingForPermission = false
    }
    OutlinedButton(onClick = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) onCall()
        else {
            waitingForPermission = true
            permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }) {
        Icon(Icons.Default.Call, null)
        Spacer(Modifier.width(5.dp))
        Text(label)
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = label != "Address")
}

private fun openWhatsApp(context: android.content.Context, phone: String, message: String) {
    val digits = phone.substringBefore('/').filter { it.isDigit() }
    if (digits.isBlank()) return
    val intl = if (digits.startsWith("0")) "93${digits.drop(1)}" else digits
    val encoded = URLEncoder.encode(message, Charsets.UTF_8.name())
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$intl?text=$encoded"))) }
}
