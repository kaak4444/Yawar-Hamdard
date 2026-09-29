package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthOnboardingScreen
import com.example.ui.common.AppStrings
import com.example.ui.common.DoctorDetailDialog
import com.example.ui.common.FacilityDetailDialog
import com.example.ui.common.ServiceDetailDialog
import com.example.ui.common.YawarTopAppBar
import com.example.ui.doctor.DoctorDashboardScreen
import com.example.ui.patient.AppointmentsScreen
import com.example.ui.patient.BookingStepperScreen
import com.example.ui.patient.CasesScreen
import com.example.ui.patient.ClaimsScreen
import com.example.ui.patient.FindCareScreen
import com.example.ui.patient.MessagesScreen
import com.example.ui.patient.PatientHomeScreen
import com.example.ui.patient.ProfileScreen
import com.example.ui.workflow.CareMessagesScreen
import com.example.ui.workflow.CallCenterDashboardScreen
import com.example.ui.workflow.HospitalDashboardScreen
import com.example.ui.workflow.HospitalPayoutsScreen
import com.example.ui.workflow.ManagerDashboardScreen
import com.example.ui.workflow.PatientCareRequestsScreen
import com.example.ui.workflow.VoiceCallMonitor
import com.example.ui.workflow.VoiceCallScreen
import com.example.ui.theme.YawarTheme
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppPaleGreen
import com.example.ui.viewmodel.YawarViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: YawarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YawarTheme {
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                val currentRole by viewModel.currentRole.collectAsState()
                val authLoading by viewModel.authLoading.collectAsState()
                val authError by viewModel.authError.collectAsState()
                val authNotice by viewModel.authNotice.collectAsState()
                val emailVerificationPending by viewModel.emailVerificationPending.collectAsState()
                val passwordResetPending by viewModel.passwordResetPending.collectAsState()
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                val lowBandwidth by viewModel.lowBandwidthMode.collectAsState()
                val unreadMessages by viewModel.unreadDirectMessageCount.collectAsState()
                val activeVoiceCall by viewModel.voiceCall.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()

                val activeDoctor by viewModel.activeDoctorDetail.collectAsState()
                val activeFacility by viewModel.activeFacilityDetail.collectAsState()
                val activeService by viewModel.activeServiceDetail.collectAsState()

                var currentPatientTab by remember { mutableIntStateOf(0) }
                var findCareInitialTab by remember { mutableIntStateOf(0) }
                var isBookingActive by remember { mutableStateOf(false) }
                var isCallScreenMinimized by remember { mutableStateOf(false) }
                var showOpeningSplash by remember { mutableStateOf(true) }

                LaunchedEffect(activeVoiceCall?.callId) {
                    isCallScreenMinimized = false
                }

                LaunchedEffect(Unit) {
                    delay(780)
                    showOpeningSplash = false
                }

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbar()
                    }
                }

                // Respect LayoutDirection based on chosen language (Dari/Pashto -> RTL)
                val layoutDirection = if (currentLanguage == AppLanguage.DARI || currentLanguage == AppLanguage.PASHTO) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    if (!isUserLoggedIn) {
                        AuthOnboardingScreen(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = { viewModel.setLanguage(it) },
                            onSignIn = viewModel::signIn,
                            onSignUp = viewModel::signUp,
                            onPasswordReset = viewModel::sendPasswordReset,
                            emailVerificationPending = emailVerificationPending,
                            passwordResetPending = passwordResetPending,
                            onVerifyEmailCode = viewModel::verifySignUpCode,
                            onResendEmailCode = viewModel::resendSignUpCode,
                            onFinishPasswordReset = viewModel::finishPasswordReset,
                            onCancelCodeFlow = viewModel::cancelEmailCodeFlow,
                            isLoading = authLoading,
                            errorMessage = authError,
                            noticeMessage = authNotice
                        )
                    } else if (activeVoiceCall != null && !isCallScreenMinimized) {
                        VoiceCallScreen(viewModel, activeVoiceCall!!) { isCallScreenMinimized = true }
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                if (!isBookingActive) {
                                    YawarTopAppBar(
                                        currentRole = currentRole,
                                        currentLanguage = currentLanguage,
                                        lowBandwidth = lowBandwidth,
                                        onLanguageSelected = { viewModel.setLanguage(it) },
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            },
                        bottomBar = {
                            if (!isBookingActive) {
                                when (currentRole) {
                                    UserRole.PATIENT -> {
                                        PatientBottomNavigation(
                                            selectedTab = currentPatientTab,
                                            onTabSelected = { currentPatientTab = it },
                                            language = currentLanguage,
                                            unreadMessages = unreadMessages
                                        )
                                    }
                                    UserRole.DOCTOR,
                                    UserRole.CALL_CENTER,
                                    UserRole.HOSPITAL,
                                    UserRole.ADMIN -> {
                                        StaffBottomNavigation(
                                            selectedTab = currentPatientTab,
                                            onTabSelected = { currentPatientTab = it },
                                            role = currentRole,
                                            language = currentLanguage,
                                            unreadMessages = unreadMessages
                                        )
                                    }
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isBookingActive) {
                                BookingStepperScreen(
                                    viewModel = viewModel,
                                    onClose = { isBookingActive = false },
                                    onViewAppointments = {
                                        isBookingActive = false
                                        currentPatientTab = 3 // Switch to Appointments tab
                                    }
                                )
                            } else {
                                when (currentRole) {
                                    UserRole.PATIENT -> {
                                        when (currentPatientTab) {
                                            0 -> PatientHomeScreen(
                                                viewModel = viewModel,
                                                onNavigateToFindCare = { tabIdx ->
                                                    findCareInitialTab = tabIdx
                                                    currentPatientTab = if (tabIdx == 1) 2 else 1
                                                },
                                                onNavigateToAppointments = { currentPatientTab = 3 },
                                                onNavigateToClaims = { currentPatientTab = 5 },
                                                onNavigateToCases = { currentPatientTab = 6 },
                                                onOpenBooking = {
                                                    viewModel.startBooking()
                                                    isBookingActive = true
                                                },
                                                onOpenYhcsMessage = { key ->
                                                    viewModel.openYhcsConversation(key)
                                                    currentPatientTab = 4
                                                }
                                            )
                                            1 -> FindCareScreen(
                                                viewModel = viewModel,
                                                initialTab = 0,
                                                onOpenBooking = { doc ->
                                                    if (doc != null) viewModel.startBooking(specialty = doc.specialty)
                                                    isBookingActive = true
                                                }
                                            )
                                            2 -> FindCareScreen(
                                                viewModel = viewModel,
                                                initialTab = 1,
                                                onOpenBooking = { doc ->
                                                    if (doc != null) viewModel.startBooking(specialty = doc.specialty)
                                                    isBookingActive = true
                                                }
                                            )
                                            3 -> PatientCareRequestsScreen(viewModel = viewModel, onOpenMessages = { currentPatientTab = 4 })
                                            4 -> CareMessagesScreen(viewModel = viewModel)
                                            5 -> ClaimsScreen(viewModel = viewModel)
                                            6 -> CasesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.DOCTOR -> {
                                        when (currentPatientTab) {
                                            0 -> DoctorDashboardScreen(
                                                viewModel = viewModel,
                                                onOpenYhcsMessage = { key ->
                                                    viewModel.openYhcsConversation(key)
                                                    currentPatientTab = 1
                                                }
                                            )
                                            1 -> CareMessagesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.CALL_CENTER -> {
                                        when (currentPatientTab) {
                                            0 -> CallCenterDashboardScreen(viewModel = viewModel, onOpenMessages = { currentPatientTab = 2 })
                                            1 -> HospitalPayoutsScreen(viewModel = viewModel, canEdit = true)
                                            2 -> CareMessagesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.HOSPITAL -> {
                                        when (currentPatientTab) {
                                            0 -> HospitalDashboardScreen(
                                                viewModel = viewModel,
                                                onOpenMessages = { currentPatientTab = 1 },
                                                onOpenYhcsMessage = { key ->
                                                    viewModel.openYhcsConversation(key)
                                                    currentPatientTab = 1
                                                }
                                            )
                                            1 -> CareMessagesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.ADMIN -> {
                                        when (currentPatientTab) {
                                            0 -> ManagerDashboardScreen(viewModel = viewModel, onOpenMessages = { currentPatientTab = 2 })
                                            1 -> HospitalPayoutsScreen(viewModel = viewModel, canEdit = true)
                                            2 -> CareMessagesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }

                            // Detail Dialogs
                            activeDoctor?.let { doc ->
                                DoctorDetailDialog(
                                    doctor = doc,
                                    language = currentLanguage,
                                    onDismiss = { viewModel.closeDoctorDetail() },
                                    onBook = {
                                        viewModel.closeDoctorDetail()
                                        viewModel.startBooking(specialty = doc.specialty)
                                        isBookingActive = true
                                    }
                                )
                            }

                            activeFacility?.let { fac ->
                                FacilityDetailDialog(
                                    facility = fac,
                                    language = currentLanguage,
                                    onDismiss = { viewModel.closeFacilityDetail() }
                                )
                            }

                            activeService?.let { srv ->
                                ServiceDetailDialog(
                                    service = srv,
                                    language = currentLanguage,
                                    onDismiss = { viewModel.closeServiceDetail() },
                                    onRequestCoordinator = {
                                        viewModel.closeServiceDetail()
                                        viewModel.startBooking(specialty = srv.titleEn)
                                        isBookingActive = true
                                    }
                                )
                            }

                        }
                    }
                    if (isUserLoggedIn) VoiceCallMonitor(viewModel)
                    if (isUserLoggedIn && activeVoiceCall != null && isCallScreenMinimized) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 42.dp)
                                .clickable { isCallScreenMinimized = false },
                            color = WhatsAppGreen,
                            shape = CircleShape,
                            shadowElevation = 5.dp
                        ) {
                            Row(Modifier.padding(horizontal = 18.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = "Return to call", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(9.dp))
                                Text("${activeVoiceCall.peerName} · ${activeVoiceCall.status}", color = Color.White, maxLines = 1)
                            }
                        }
                    }
                    }
                    }

                    AnimatedVisibility(
                        visible = showOpeningSplash,
                        modifier = Modifier.fillMaxSize(),
                        enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                            scaleIn(initialScale = 1.025f, animationSpec = tween(durationMillis = 500)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 350)) +
                            scaleOut(targetScale = 1.025f, animationSpec = tween(durationMillis = 350))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0162D1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.size(240.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class YawarNavItem(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
    val badgeCount: Int = 0
)

/**
 * One bottom bar shared by both roles. The patient and staff variants were
 * previously written out item-by-item with duplicated colour blocks and
 * English-only labels, which broke the bilingual promise of the app.
 */
@Composable
private fun YawarNavBar(
    items: List<YawarNavItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    accent: Color,
    accentContainer: Color
) {
    val scheme = MaterialTheme.colorScheme
    NavigationBar(
        modifier = Modifier.border(1.dp, scheme.outlineVariant),
        containerColor = scheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Box {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = if (item.badgeCount > 0) "${item.contentDescription}, ${item.badgeCount} unread" else item.contentDescription,
                            modifier = Modifier.size(24.dp)
                        )
                        if (item.badgeCount > 0) {
                            Text(
                                item.badgeCount.coerceAtMost(99).toString(),
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .background(WhatsAppGreen, CircleShape)
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                color = Color.White,
                                fontSize = 8.sp,
                                lineHeight = 9.sp
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = accentContainer,
                    unselectedIconColor = scheme.onSurfaceVariant,
                    unselectedTextColor = scheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun PatientBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    language: AppLanguage,
    unreadMessages: Int = 0
) {
    val scheme = MaterialTheme.colorScheme
    val items = listOf(
        YawarNavItem(Icons.Default.Home, AppStrings.getHome(language), "Home"),
        YawarNavItem(Icons.Default.Person, AppStrings.getFindDoctor(language), "Doctors"),
        YawarNavItem(Icons.Default.LocalHospital, AppStrings.getFindHospital(language), "Hospitals"),
        YawarNavItem(Icons.Default.CalendarMonth, AppStrings.getAppointments(language), "Appointments"),
        YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages", unreadMessages)
    )
    YawarNavBar(
        items = items,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        accent = if (selectedTab == 4) WhatsAppGreen else scheme.primary,
        accentContainer = if (selectedTab == 4) WhatsAppPaleGreen else scheme.primaryContainer
    )
}

@Composable
fun StaffBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    role: UserRole,
    language: AppLanguage,
    unreadMessages: Int = 0
) {
    val scheme = MaterialTheme.colorScheme
    val items = when (role) {
        UserRole.ADMIN -> listOf(
            YawarNavItem(Icons.Default.Home, "Manager", "Manager dashboard"),
            YawarNavItem(Icons.Default.ReceiptLong, "Hospital payouts", "Hospital payouts"),
            YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages", unreadMessages),
            YawarNavItem(Icons.Default.Person, AppStrings.getProfile(language), "Account")
        )
        UserRole.CALL_CENTER -> listOf(
            YawarNavItem(Icons.Default.Home, "Case queue", "Call center queue"),
            YawarNavItem(Icons.Default.ReceiptLong, "Hospital payouts", "Hospital payouts"),
            YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages", unreadMessages),
            YawarNavItem(Icons.Default.Person, AppStrings.getProfile(language), "Account")
        )
        UserRole.HOSPITAL -> listOf(
            YawarNavItem(Icons.Default.LocalHospital, "Referrals", "Hospital referrals"),
            YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages", unreadMessages),
            YawarNavItem(Icons.Default.Person, AppStrings.getProfile(language), "Account")
        )
        else -> listOf(
            YawarNavItem(Icons.Default.Home, AppStrings.getSchedule(language), "Dashboard"),
            YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages", unreadMessages),
            YawarNavItem(Icons.Default.Person, AppStrings.getProfile(language), "Account")
        )
    }
    val messagesTab = when (role) {
        UserRole.ADMIN, UserRole.CALL_CENTER -> 2
        UserRole.HOSPITAL, UserRole.DOCTOR -> 1
        else -> -1
    }
    YawarNavBar(
        items = items,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        accent = if (selectedTab == messagesTab) WhatsAppGreen else scheme.secondary,
        accentContainer = if (selectedTab == messagesTab) WhatsAppPaleGreen else scheme.secondaryContainer
    )
}
