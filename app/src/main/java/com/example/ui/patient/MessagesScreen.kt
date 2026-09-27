package com.example.ui.patient

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.common.ChatDateDivider
import com.example.ui.common.rememberChatPalette
import com.example.ui.common.chatDoodleWallpaper
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.PaleBlue
import com.example.ui.theme.PaleGreen
import com.example.ui.theme.Slate
import com.example.ui.theme.YawarBlue
import com.example.ui.theme.YawarNavy
import com.example.ui.theme.ChatBubbleOut
import com.example.ui.theme.ChatCanvas
import com.example.ui.theme.ChatOnlineDot
import com.example.ui.theme.ChatSystemBg
import com.example.ui.theme.ChatSystemBorder
import com.example.ui.theme.ChatSystemText
import com.example.ui.theme.ChatSystemTextDeep
import com.example.ui.theme.ChatTickRead
import com.example.ui.theme.ChipBg
import com.example.ui.theme.DangerText
import com.example.ui.viewmodel.YawarViewModel

data class ConversationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val roleBadge: String,
    val avatarInitials: String,
    val avatarColor: Color,
    val isOnline: Boolean,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0
)

@Composable
fun MessagesScreen(
    viewModel: YawarViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var privacyModeEnabled by remember { mutableStateOf(true) }

    val conversations = remember(messages) {
        listOf(
            ConversationItem(
                id = "conv_coord",
                title = "Farhad (YHCS Senior Coordinator)",
                subtitle = "Care Coordination Desk • YHCS Kabul",
                roleBadge = "Care Officer",
                avatarInitials = "FH",
                avatarColor = ClinicalGreen,
                isOnline = true,
                lastMessage = messages.filter { it.conversationId == "conv_coord" }.lastOrNull()?.content
                    ?: "Hospital reception confirmed slot with Dr. Momand.",
                timestamp = "10:05 AM",
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_momand",
                title = "Dr. Abdul Wasi Momand",
                subtitle = "Sonology & General Medicine • Khalid Basir Hospital",
                roleBadge = "Specialist",
                avatarInitials = "WM",
                avatarColor = YawarBlue,
                isOnline = true,
                lastMessage = messages.filter { it.conversationId == "conv_momand" }.lastOrNull()?.content
                    ?: "Fasting for 6 hours is requested prior to upper abdominal ultrasound scan.",
                timestamp = "Yesterday",
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_safir",
                title = "Dr. Safir Khan",
                subtitle = "Pediatrics Specialist • Nang Curative Hospital",
                roleBadge = "Specialist",
                avatarInitials = "SK",
                avatarColor = DeepGreen,
                isOnline = false,
                lastMessage = messages.filter { it.conversationId == "conv_safir" }.lastOrNull()?.content
                    ?: "Please bring child's vaccination card and record of previous allergies.",
                timestamp = "20 Sep",
                unreadCount = 0
            )
        )
    }

    if (selectedConversationId == null) {
        ConversationListView(
            conversations = conversations,
            onSelect = { selectedConversationId = it },
            privacyMode = privacyModeEnabled,
            onTogglePrivacy = { privacyModeEnabled = it }
        )
    } else {
        val currentConv = conversations.firstOrNull { it.id == selectedConversationId }
            ?: conversations.first()

        ActiveChatView(
            conversation = currentConv,
            messages = messages.filter { it.conversationId == currentConv.id },
            onBack = { selectedConversationId = null },
            privacyMode = privacyModeEnabled,
            onTogglePrivacy = { privacyModeEnabled = it },
            onSendMessage = { text, type, attachName, attachSize, voiceSec ->
                viewModel.sendMessage(
                    content = text,
                    conversationId = currentConv.id,
                    messageType = type,
                    attachmentName = attachName,
                    attachmentSize = attachSize,
                    voiceDurationSec = voiceSec
                )
            }
        )
    }
}

// ----------------- CONVERSATION LIST VIEW -----------------

@Composable
fun ConversationListView(
    conversations: List<ConversationItem>,
    onSelect: (String) -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Coordinator, 2: Doctors

    val filteredList = conversations.filter { conv ->
        val matchesQuery = conv.title.contains(searchQuery, ignoreCase = true) ||
                conv.subtitle.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            1 -> conv.id == "conv_coord"
            2 -> conv.id.startsWith("conv_") && conv.id != "conv_coord"
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, BorderColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clinical Messages & Coordination",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YawarNavy,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "End-to-end encrypted medical communication",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate,
                            fontSize = 12.sp
                        )
                    )
                }

                // Privacy Indicator
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (privacyMode) PaleGreen else ChipBg)
                        .clickable { onTogglePrivacy(!privacyMode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (privacyMode) Icons.Default.Lock else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (privacyMode) ClinicalGreen else Slate,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (privacyMode) "Private" else "Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (privacyMode) DeepGreen else Slate
                        )
                    }
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search messages, doctors, or coordinators...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate, modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = ChipBg,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = ClinicalGreen
                ),
                singleLine = true
            )
        }

        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("All Care Threads", "YHCS Coordinator", "Specialist Doctors")
            items(chips.indices.toList()) { idx ->
                FilterChip(
                    selected = selectedFilter == idx,
                    onClick = { selectedFilter = idx },
                    label = { Text(chips[idx], fontSize = 12.sp, fontWeight = if (selectedFilter == idx) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PaleGreen,
                        selectedLabelColor = DeepGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Privacy Lock Screen Protection Card
        if (privacyMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFFEFF6FF))
                    .border(1.dp, Color(0xFFBFDBFE), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = YawarBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lock-screen privacy active: Medical diagnoses & lab reports hidden from lock screen notifications.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Conversation List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(filteredList) { conv ->
                ConversationRow(
                    conversation = conv,
                    privacyMode = privacyMode,
                    onClick = { onSelect(conv.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationRow(
    conversation: ConversationItem,
    privacyMode: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Online dot
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(conversation.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(ChatOnlineDot)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.title,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PaleBlue)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = conversation.roleBadge,
                                color = YawarNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = conversation.timestamp,
                        color = Slate,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (privacyMode) "New encrypted clinical communication" else conversation.lastMessage,
                        color = Slate,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(ClinicalGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------- ACTIVE CHAT VIEW -----------------

@Composable
fun ActiveChatView(
    conversation: ConversationItem,
    messages: List<MessageEntity>,
    onBack: () -> Unit,
    privacyMode: Boolean,
    onTogglePrivacy: (Boolean) -> Unit,
    onSendMessage: (String, String, String, String, Int) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var messageText by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<MessageEntity?>(null) }
    var isSimulatingTyping by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordDurationSec by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice record timer simulation
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordDurationSec = 0
            while (isRecordingVoice) {
                kotlinx.coroutines.delay(1000)
                recordDurationSec++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberChatPalette().canvas) // WhatsApp canvas, light or dark
    ) {
        // Chat Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, BorderColor)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YawarNavy)
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(conversation.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(conversation.avatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (conversation.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ChatOnlineDot)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = conversation.title,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isSimulatingTyping) "typing..." else if (conversation.isOnline) "Online • YHCS Secure Channel" else "Offline",
                            color = if (isSimulatingTyping) ClinicalGreen else Slate,
                            fontSize = 11.sp,
                            fontWeight = if (isSimulatingTyping) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Header Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Direct Call Support
                    IconButton(onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+93707438303")
                        }
                        context.startActivity(dialIntent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Secure Call",
                            tint = ClinicalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Slate)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Slate,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (privacyMode) "Disable Lock-screen Privacy" else "Enable Lock-screen Privacy", fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    onTogglePrivacy(!privacyMode)
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Call 24/7 Operations Desk (+93 707 438 303)", fontSize = 12.sp) },
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:+93707438303")
                                    }
                                    context.startActivity(dialIntent)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Encryption Badge Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(ChatSystemBg)
                    .border(0.5.dp, ChatSystemBorder, MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ChatSystemText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Messages and clinical attachments are end-to-end encrypted.",
                        color = ChatSystemTextDeep,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .chatDoodleWallpaper(tint = rememberChatPalette().wallpaperTint),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { ChatDateDivider("Today") }
            items(messages) { msg ->
                val isOutgoing = msg.senderRole == "PATIENT"
                ChatBubble(
                    message = msg,
                    isOutgoing = isOutgoing,
                    onLongClick = { selectedMessageForOptions = msg },
                    onViewDocument = { title, size -> showPreviewDialog = Pair(title, size) }
                )
            }
        }

        // Composer & Voice Bar
        ChatComposer(
            text = messageText,
            onTextChanged = { messageText = it },
            isRecording = isRecordingVoice,
            recordingSec = recordDurationSec,
            onStartRecord = { isRecordingVoice = true },
            onStopRecord = {
                val sec = recordDurationSec
                isRecordingVoice = false
                if (sec > 0) {
                    onSendMessage("Voice clinical note (${sec}s)", "VOICE", "Clinical_Voice_Note.aac", "${sec * 8} KB", sec)
                }
            },
            onCancelRecord = { isRecordingVoice = false },
            onOpenAttachment = { showAttachSheet = true },
            onSend = {
                if (messageText.isNotBlank()) {
                    val txt = messageText
                    messageText = ""
                    onSendMessage(txt, "TEXT", "", "", 0)
                }
            }
        )
    }

    // Attachment Picker Modal
    if (showAttachSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachSheet = false },
            onSelectAttachment = { type, name, size ->
                showAttachSheet = false
                showPreviewDialog = Pair(name, size)
            }
        )
    }

    // Document Preview Before Sending
    showPreviewDialog?.let { (docName, docSize) ->
        DocumentPreviewDialog(
            docName = docName,
            docSize = docSize,
            onDismiss = { showPreviewDialog = null },
            onSend = { caption ->
                showPreviewDialog = null
                onSendMessage(
                    if (caption.isNotBlank()) caption else "Attached medical document: $docName",
                    "DOCUMENT",
                    docName,
                    docSize,
                    0
                )
            }
        )
    }

    // Message Options Modal
    selectedMessageForOptions?.let { msg ->
        MessageOptionsDialog(
            message = msg,
            onDismiss = { selectedMessageForOptions = null },
            onCopy = {
                clipboard.setText(AnnotatedString(msg.content))
                selectedMessageForOptions = null
            }
        )
    }
}

// ----------------- CHAT BUBBLE -----------------

@Composable
fun ChatBubble(
    message: MessageEntity,
    isOutgoing: Boolean,
    onLongClick: () -> Unit,
    onViewDocument: (String, String) -> Unit
) {
    val palette = rememberChatPalette()
    val bubbleColor = if (isOutgoing) palette.bubbleOut else palette.bubbleIn
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLongClick),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .border(0.5.dp, palette.bubbleBorder, bubbleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (!isOutgoing) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = YawarBlue,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                when (message.messageType) {
                    "VOICE" -> {
                        VoiceNoteBubbleContent(
                            durationSec = if (message.voiceDurationSec > 0) message.voiceDurationSec else 24,
                            isOutgoing = isOutgoing
                        )
                    }
                    "DOCUMENT" -> {
                        DocumentBubbleContent(
                            docName = message.attachmentName.ifBlank { "Diagnostic_Report.pdf" },
                            docSize = message.attachmentSize.ifBlank { "1.2 MB" },
                            caption = message.content,
                            onView = { onViewDocument(message.attachmentName, message.attachmentSize) }
                        )
                    }
                    else -> {
                        Text(
                            text = message.content,
                            color = palette.bubbleText,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp and Checkmark
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "10:05 AM",
                        color = Slate,
                        fontSize = 11.sp
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = ChatTickRead, // Blue double check
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceNoteBubbleContent(
    durationSec: Int,
    isOutgoing: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableIntStateOf(1) } // 1x, 2x

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) ClinicalGreen else YawarBlue)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform bars
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barHeights = listOf(8, 14, 20, 12, 16, 22, 18, 10, 16, 24, 14, 18, 8, 12, 16, 20, 14, 10)
            barHeights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isPlaying) ClinicalGreen else Slate.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("0:%02d", durationSec),
                fontSize = 11.sp,
                color = Slate
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { speedMultiplier = if (speedMultiplier == 1) 2 else 1 }
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "${speedMultiplier}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = YawarNavy
                )
            }
        }
    }
}

@Composable
fun DocumentBubbleContent(
    docName: String,
    docSize: String,
    caption: String,
    onView: () -> Unit
) {
    Column {
        Card(
            shape = MaterialTheme.shapes.extraSmall,
            colors = CardDefaults.cardColors(containerColor = PaleBlue),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onView)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(YawarNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = docName,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$docSize • Tap to preview",
                        color = Slate,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = YawarBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (caption.isNotBlank() && caption != "Attached medical document: $docName") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption,
                color = Ink,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}

// ----------------- CHAT COMPOSER -----------------

@Composable
fun ChatComposer(
    text: String,
    onTextChanged: (String) -> Unit,
    isRecording: Boolean,
    recordingSec: Int,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onCancelRecord: () -> Unit,
    onOpenAttachment: () -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChipBg)
            .border(0.5.dp, BorderColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        if (isRecording) {
            // Recording State Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DangerText)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recording clinical voice note: 0:${String.format("%02d", recordingSec)}",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onCancelRecord) {
                        Text("Cancel", color = Slate, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = onStopRecord,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ClinicalGreen)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Send Voice Note", tint = Color.White)
                    }
                }
            }
        } else {
            // Standard Composer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach button (+)
                IconButton(onClick = onOpenAttachment) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach Document",
                        tint = Slate,
                        modifier = Modifier.size(24.dp)
                    )
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = { Text("Type a secure message...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = BorderColor,
                        focusedBorderColor = ClinicalGreen
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(4.dp))

                if (text.isNotBlank()) {
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ClinicalGreen)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onStartRecord,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(YawarNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice Note",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------- MODALS & DIALOGS -----------------

@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSelectAttachment: (type: String, name: String, size: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Clinical Documents", fontWeight = FontWeight.Bold, color = YawarNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AttachmentOptionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera Capture",
                    subtitle = "Capture prescription, lab slip, or skin lesion",
                    onClick = { onSelectAttachment("IMAGE", "Prescription_Photo.jpg", "850 KB") }
                )
                AttachmentOptionRow(
                    icon = Icons.Default.AttachFile,
                    title = "Diagnostic Lab Report",
                    subtitle = "Upload blood count (CBC), ultrasound, or pathology report",
                    onClick = { onSelectAttachment("DOCUMENT", "Ultrasound_Report_KhalidBasir.pdf", "1.4 MB") }
                )
                AttachmentOptionRow(
                    icon = Icons.Default.Security,
                    title = "Referral & Member Card",
                    subtitle = "Attach Tazkira or YHCS Corporate Member document",
                    onClick = { onSelectAttachment("DOCUMENT", "YHCS_Member_Coverage_Card.pdf", "540 KB") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate)
            }
        }
    )
}

@Composable
fun AttachmentOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PaleGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ClinicalGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
            Text(subtitle, color = Slate, fontSize = 11.sp)
        }
    }
}

@Composable
fun DocumentPreviewDialog(
    docName: String,
    docSize: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var caption by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Document Sharing", fontWeight = FontWeight.Bold, color = YawarNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(PaleBlue)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = YawarNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(docName, fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
                            Text("Size: $docSize • Verified Document Format", color = Slate, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Add clinical note or instructions (optional)...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(caption) },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalGreen)
            ) {
                Text("Send Document", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate)
            }
        }
    )
}

@Composable
fun MessageOptionsDialog(
    message: MessageEntity,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Actions", fontWeight = FontWeight.Bold, color = YawarNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
                    Text("Copy Text Content", color = Ink, modifier = Modifier.fillMaxWidth())
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete for Me", color = DangerText, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Slate) }
        }
    )
}
