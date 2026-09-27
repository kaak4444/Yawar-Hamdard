package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import com.example.ui.theme.YawarTheme
import com.example.ui.viewmodel.YawarViewModel

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
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()

                val activeDoctor by viewModel.activeDoctorDetail.collectAsState()
                val activeFacility by viewModel.activeFacilityDetail.collectAsState()
                val activeService by viewModel.activeServiceDetail.collectAsState()

                var currentPatientTab by remember { mutableIntStateOf(0) }
                var findCareInitialTab by remember { mutableIntStateOf(0) }
                var isBookingActive by remember { mutableStateOf(false) }

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
                                            language = currentLanguage
                                        )
                                    }
                                    UserRole.DOCTOR,
                                    UserRole.ADMIN -> {
                                        // For Doctor and Admin modes, quick switch tabs (Dashboard, Messages, Profile)
                                        StaffBottomNavigation(
                                            selectedTab = currentPatientTab,
                                            onTabSelected = { currentPatientTab = it },
                                            role = currentRole,
                                            language = currentLanguage
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
                                                }
                                            )
                                            1 -> FindCareScreen(
                                                viewModel = viewModel,
                                                initialTab = 0,
                                                onOpenBooking = { doc ->
                                                    viewModel.startBooking(doc)
                                                    isBookingActive = true
                                                }
                                            )
                                            2 -> FindCareScreen(
                                                viewModel = viewModel,
                                                initialTab = 1,
                                                onOpenBooking = { doc ->
                                                    viewModel.startBooking(doc)
                                                    isBookingActive = true
                                                }
                                            )
                                            3 -> AppointmentsScreen(
                                                viewModel = viewModel,
                                                onBookNew = {
                                                    viewModel.startBooking()
                                                    isBookingActive = true
                                                }
                                            )
                                            4 -> MessagesScreen(viewModel = viewModel)
                                            5 -> ClaimsScreen(viewModel = viewModel)
                                            6 -> CasesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.DOCTOR -> {
                                        when (currentPatientTab) {
                                            0 -> DoctorDashboardScreen(viewModel = viewModel)
                                            1 -> MessagesScreen(viewModel = viewModel)
                                            else -> ProfileScreen(viewModel = viewModel)
                                        }
                                    }

                                    UserRole.ADMIN -> {
                                        when (currentPatientTab) {
                                            0 -> AdminDashboardScreen(viewModel = viewModel)
                                            1 -> CasesScreen(viewModel = viewModel)
                                            2 -> MessagesScreen(viewModel = viewModel)
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
                                        viewModel.startBooking(doc)
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
                    }
                }
            }
        }
    }
}

private data class YawarNavItem(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
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
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        modifier = Modifier.size(24.dp)
                    )
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
    language: AppLanguage
) {
    val scheme = MaterialTheme.colorScheme
    val items = listOf(
        YawarNavItem(Icons.Default.Home, AppStrings.getHome(language), "Home"),
        YawarNavItem(Icons.Default.Person, AppStrings.getFindDoctor(language), "Doctors"),
        YawarNavItem(Icons.Default.LocalHospital, AppStrings.getFindHospital(language), "Hospitals"),
        YawarNavItem(Icons.Default.CalendarMonth, AppStrings.getAppointments(language), "Appointments"),
        YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages")
    )
    YawarNavBar(
        items = items,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        accent = scheme.primary,
        accentContainer = scheme.primaryContainer
    )
}

@Composable
fun StaffBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    role: UserRole,
    language: AppLanguage
) {
    val scheme = MaterialTheme.colorScheme
    val isAdmin = role == UserRole.ADMIN
    val items = buildList {
        add(
            YawarNavItem(
                icon = Icons.Default.Home,
                label = if (isAdmin) AppStrings.getDashboard(language) else AppStrings.getSchedule(language),
                contentDescription = "Dashboard"
            )
        )
        if (isAdmin) {
            add(
                YawarNavItem(
                    icon = Icons.Default.MedicalServices,
                    label = AppStrings.getCoordinationCases(language),
                    contentDescription = "Cases"
                )
            )
        }
        add(YawarNavItem(Icons.Default.Chat, AppStrings.getMessages(language), "Messages"))
        add(YawarNavItem(Icons.Default.Person, AppStrings.getProfile(language), "Account"))
    }
    YawarNavBar(
        items = items,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        accent = scheme.secondary,
        accentContainer = scheme.secondaryContainer
    )
}
