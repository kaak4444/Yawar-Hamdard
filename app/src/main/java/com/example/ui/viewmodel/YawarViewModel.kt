package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppointmentEntity
import com.example.data.local.ClaimEntity
import com.example.data.local.CoordinationCaseEntity
import com.example.data.local.DoctorEntity
import com.example.data.local.FacilityEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.YawarDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.data.model.ServiceItem
import com.example.data.model.UserRole
import com.example.data.model.VisitType
import com.example.data.repository.YawarRepository
import com.example.data.seed.DataSeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class BookingDraft(
    val specialty: String = "Cardiology",
    val reasonForCare: String = "",
    val symptomsSummary: String = "",
    val urgency: String = "Routine",
    val province: String = "Kabul",
    val visitType: VisitType = VisitType.IN_PERSON,
    val facilityId: String = "",
    val facilityName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val isFirstSuitableDoctor: Boolean = true,
    val appointmentDate: String = "24 Sep 2026",
    val timeSlot: String = "10:00 AM",
    val corporateMemberId: String = "YHCS-CORP-9021",
    val attachedDocs: List<String> = emptyList(),
    val contactPreference: String = "WhatsApp & In-App",
    val consentGiven: Boolean = true
)

class YawarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: YawarRepository

    init {
        val database = YawarDatabase.getDatabase(application)
        repository = YawarRepository(database.yawarDao())
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    // Role & Language State
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.PATIENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun login(role: UserRole) {
        _currentRole.value = role
        _isUserLoggedIn.value = true
    }

    fun logout() {
        _isUserLoggedIn.value = false
    }

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _lowBandwidthMode = MutableStateFlow(false)
    val lowBandwidthMode: StateFlow<Boolean> = _lowBandwidthMode.asStateFlow()

    private val _selectedProvince = MutableStateFlow("All Provinces")
    val selectedProvince: StateFlow<String> = _selectedProvince.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpecialtyFilter = MutableStateFlow("All")
    val selectedSpecialtyFilter: StateFlow<String> = _selectedSpecialtyFilter.asStateFlow()

    // Database Flows
    val doctors: StateFlow<List<DoctorEntity>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facilities: StateFlow<List<FacilityEntity>> = repository.allFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val claims: StateFlow<List<ClaimEntity>> = repository.allClaims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cases: StateFlow<List<CoordinationCaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val services: List<ServiceItem> = DataSeed.yawarServices

    // Filtered lists
    val filteredDoctors: StateFlow<List<DoctorEntity>> = combine(
        doctors, _searchQuery, _selectedProvince, _selectedSpecialtyFilter
    ) { list, query, province, specialty ->
        list.filter { doc ->
            val matchesQuery = query.isBlank() ||
                    doc.name.contains(query, ignoreCase = true) ||
                    doc.specialty.contains(query, ignoreCase = true) ||
                    doc.hospitalAffiliation.contains(query, ignoreCase = true) ||
                    doc.city.contains(query, ignoreCase = true)
            val matchesProvince = province == "All Provinces" || doc.province.equals(province, ignoreCase = true)
            val matchesSpecialty = specialty == "All" || doc.specialty.equals(specialty, ignoreCase = true)
            matchesQuery && matchesProvince && matchesSpecialty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredFacilities: StateFlow<List<FacilityEntity>> = combine(
        facilities, _searchQuery, _selectedProvince
    ) { list, query, province ->
        list.filter { fac ->
            val matchesQuery = query.isBlank() ||
                    fac.name.contains(query, ignoreCase = true) ||
                    fac.facilityType.contains(query, ignoreCase = true) ||
                    fac.address.contains(query, ignoreCase = true)
            val matchesProvince = province == "All Provinces" || fac.province.equals(province, ignoreCase = true)
            matchesQuery && matchesProvince
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Booking Flow State
    private val _bookingDraft = MutableStateFlow(BookingDraft())
    val bookingDraft: StateFlow<BookingDraft> = _bookingDraft.asStateFlow()

    private val _bookingStep = MutableStateFlow(1)
    val bookingStep: StateFlow<Int> = _bookingStep.asStateFlow()

    private val _lastSubmittedReference = MutableStateFlow<String?>(null)
    val lastSubmittedReference: StateFlow<String?> = _lastSubmittedReference.asStateFlow()

    // Navigation and inspection state
    private val _activeDoctorDetail = MutableStateFlow<DoctorEntity?>(null)
    val activeDoctorDetail: StateFlow<DoctorEntity?> = _activeDoctorDetail.asStateFlow()

    private val _activeFacilityDetail = MutableStateFlow<FacilityEntity?>(null)
    val activeFacilityDetail: StateFlow<FacilityEntity?> = _activeFacilityDetail.asStateFlow()

    private val _activeServiceDetail = MutableStateFlow<ServiceItem?>(null)
    val activeServiceDetail: StateFlow<ServiceItem?> = _activeServiceDetail.asStateFlow()

    private val _activeAppointmentDetail = MutableStateFlow<AppointmentEntity?>(null)
    val activeAppointmentDetail: StateFlow<AppointmentEntity?> = _activeAppointmentDetail.asStateFlow()

    // Toast / Banner notice
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun setRole(role: UserRole) {
        _currentRole.value = role
        showSnackbar("Switched to ${role.displayName} mode")
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        showSnackbar("Language set to ${language.nativeName}")
    }

    fun toggleLowBandwidth(enabled: Boolean) {
        _lowBandwidthMode.value = enabled
        showSnackbar(if (enabled) "Low-bandwidth mode active: saving data" else "Standard bandwidth mode")
    }

    fun setProvince(province: String) {
        _selectedProvince.value = province
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSpecialtyFilter(specialty: String) {
        _selectedSpecialtyFilter.value = specialty
    }

    fun openDoctorDetail(doctor: DoctorEntity) {
        _activeDoctorDetail.value = doctor
    }

    fun closeDoctorDetail() {
        _activeDoctorDetail.value = null
    }

    fun openFacilityDetail(facility: FacilityEntity) {
        _activeFacilityDetail.value = facility
    }

    fun closeFacilityDetail() {
        _activeFacilityDetail.value = null
    }

    fun openServiceDetail(service: ServiceItem) {
        _activeServiceDetail.value = service
    }

    fun closeServiceDetail() {
        _activeServiceDetail.value = null
    }

    fun openAppointmentDetail(appointment: AppointmentEntity) {
        _activeAppointmentDetail.value = appointment
    }

    fun closeAppointmentDetail() {
        _activeAppointmentDetail.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Booking Flow controls
    fun startBooking(doctor: DoctorEntity? = null, specialty: String = "Cardiology") {
        _bookingDraft.value = BookingDraft(
            specialty = doctor?.specialty ?: specialty,
            doctorId = doctor?.id ?: "",
            doctorName = doctor?.name ?: "First Suitable Specialist",
            facilityName = doctor?.hospitalAffiliation ?: "FMIC Tertiary Hospital",
            isFirstSuitableDoctor = (doctor == null)
        )
        _bookingStep.value = 1
        _lastSubmittedReference.value = null
    }

    fun updateBookingDraft(transform: (BookingDraft) -> BookingDraft) {
        _bookingDraft.value = transform(_bookingDraft.value)
    }

    fun nextBookingStep() {
        if (_bookingStep.value < 8) {
            _bookingStep.value += 1
        }
    }

    fun prevBookingStep() {
        if (_bookingStep.value > 1) {
            _bookingStep.value -= 1
        }
    }

    fun submitBooking() {
        viewModelScope.launch {
            val draft = _bookingDraft.value
            val referenceId = "YHCS-2026-" + Random.nextInt(1000, 9999)
            val newAppointment = AppointmentEntity(
                id = referenceId,
                patientName = "Ahmad Shah",
                patientPhone = "+93 70 123 4567",
                isDependent = false,
                dependentName = "",
                dependentRelation = "",
                specialty = draft.specialty,
                reasonForCare = if (draft.reasonForCare.isNotBlank()) draft.reasonForCare else "Clinical consultation and assessment",
                symptomsSummary = draft.symptomsSummary,
                urgency = draft.urgency,
                visitType = draft.visitType,
                province = draft.province,
                facilityId = draft.facilityId,
                facilityName = if (draft.facilityName.isNotBlank()) draft.facilityName else "French Medical Institute for Mothers & Children (FMIC)",
                doctorId = draft.doctorId,
                doctorName = if (draft.doctorName.isNotBlank()) draft.doctorName else "First Available Specialist",
                appointmentDate = draft.appointmentDate,
                timeSlot = draft.timeSlot,
                corporateMemberId = draft.corporateMemberId,
                attachedDocuments = if (draft.attachedDocs.isNotEmpty()) draft.attachedDocs else listOf("Patient_Clinical_Brief.pdf"),
                contactPreference = draft.contactPreference,
                status = AppointmentStatus.SUBMITTED,
                coordinatorNotes = "Auto-dispatched to callcenter@yawarconsulting.com. Pending coordinator review.",
                doctorPreparationNotes = "Bring all existing medication packages and prior laboratory reports.",
                emailAlertSentTo = "callcenter@yawarconsulting.com",
                createdAtTimestamp = System.currentTimeMillis()
            )

            repository.submitAppointment(newAppointment)
            _lastSubmittedReference.value = referenceId
            _bookingStep.value = 9 // Step 9: Confirmation

            // Also log an automated coordinator intake message
            repository.sendMessage(
                content = "Salam. Your booking request ($referenceId) for ${newAppointment.specialty} at ${newAppointment.facilityName} is under review. Operational alert sent to callcenter@yawarconsulting.com.",
                senderRole = "COORDINATOR",
                senderName = "Farhad (YHCS Coordinator)"
            )
        }
    }

    // Doctor Actions
    fun doctorConfirmAppointment(id: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, AppointmentStatus.CONFIRMED, "Confirmed by treating physician.")
            showSnackbar("Appointment $id confirmed!")
        }
    }

    fun doctorDeclineAppointment(id: String, reason: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, AppointmentStatus.CANCELLED, "Provider unavailable: $reason")
            showSnackbar("Appointment declined with reason: $reason")
        }
    }

    fun doctorCompleteVisit(id: String, summary: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, AppointmentStatus.COMPLETED, "Visit completed. Summary: $summary")
            showSnackbar("Visit marked as completed with clinical notes.")
        }
    }

    // Admin Actions
    fun adminAssignAndConfirm(id: String, coordinatorName: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(
                id,
                AppointmentStatus.CONFIRMED,
                "Assigned to $coordinatorName. Verified with hospital reception and patient notified."
            )
            showSnackbar("Appointment $id confirmed by $coordinatorName")
        }
    }

    fun adminRequestReschedule(id: String, newDate: String, newSlot: String) {
        viewModelScope.launch {
            repository.rescheduleAppointment(id, newDate, newSlot)
            showSnackbar("Reschedule proposed for $id: $newDate $newSlot")
        }
    }

    fun adminApproveClaim(id: String) {
        viewModelScope.launch {
            repository.updateClaimStatus(id, ClaimStatus.APPROVED)
            showSnackbar("Claim $id approved for direct settlement.")
        }
    }

    fun adminVerifyDoctor(doctorId: String) {
        verifyDoctor(doctorId, true)
    }

    fun verifyDoctor(doctorId: String, verified: Boolean = true) {
        viewModelScope.launch {
            val doc = doctors.value.firstOrNull { it.id == doctorId }
            if (doc != null) {
                val updated = doc.copy(
                    isVerified = verified,
                    verificationStatus = if (verified) "Verified Specialist" else "Pending Verification"
                )
                repository.updateDoctor(updated)
                showSnackbar(if (verified) "${doc.name} verified and approved for clinical referrals!" else "${doc.name} verification revoked.")
            }
        }
    }

    // Auth & Onboarding state
    private val _onboardingComplete = MutableStateFlow(true)
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()

    fun completeOnboarding(role: UserRole) {
        _currentRole.value = role
        _onboardingComplete.value = true
        showSnackbar("Logged in as ${role.displayName}")
    }

    fun openOnboarding() {
        _onboardingComplete.value = false
    }

    fun submitNewClaim(claimType: String, provider: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val claimId = "CLM-2026-" + Random.nextInt(1000, 9999)
            val claim = ClaimEntity(
                id = claimId,
                patientName = "Ahmad Shah",
                claimType = claimType,
                providerOrHospital = provider,
                dateOfService = "Today",
                invoiceAmountAf = amount,
                approvedAmountAf = 0.0,
                status = ClaimStatus.SUBMITTED,
                documentTypes = listOf("Itemized Invoice", "Doctor Prescription"),
                remarks = notes.ifBlank { "Claim submitted via mobile app." },
                submissionDate = "22 Sep 2026"
            )
            repository.submitClaim(claim)
            showSnackbar("Claim $claimId registered! Operational alert sent to callcenter@yawarconsulting.com")
        }
    }

    fun sendMessage(
        content: String,
        conversationId: String = "conv_coord",
        messageType: String = "TEXT",
        attachmentName: String = "",
        attachmentSize: String = "",
        voiceDurationSec: Int = 0
    ) {
        if (content.isBlank() && messageType == "TEXT") return
        viewModelScope.launch {
            val role = _currentRole.value
            val senderName = when (role) {
                UserRole.PATIENT -> "Ahmad Shah (Patient)"
                UserRole.DOCTOR -> "Dr. Abdul Wasi Momand"
                UserRole.ADMIN -> "Farhad (Care Coordinator)"
            }
            repository.sendMessage(
                content = content,
                senderRole = role.name,
                senderName = senderName,
                conversationId = conversationId,
                messageType = messageType,
                attachmentName = attachmentName,
                attachmentSize = attachmentSize,
                voiceDurationSec = voiceDurationSec,
                deliveryStatus = "READ"
            )

            // 1-to-1 direct recipient response (Strictly scoped to the conversation recipient)
            if (role == UserRole.PATIENT) {
                kotlinx.coroutines.delay(1200)
                when (conversationId) {
                    "conv_coord" -> {
                        repository.sendMessage(
                            content = "Tashakor for reaching out. A Yawar health coordinator has received your message and will respond shortly. For urgent emergency support, please call +93 707 438 303.",
                            senderRole = "COORDINATOR",
                            senderName = "Farhad (Care Coordinator)",
                            conversationId = conversationId
                        )
                    }
                    "conv_momand" -> {
                        repository.sendMessage(
                            content = "Salam Ahmad Shah. I have received your message regarding your ultrasound appointment. I will review your previous reports before Saturday's examination.",
                            senderRole = "DOCTOR",
                            senderName = "Dr. Abdul Wasi Momand",
                            conversationId = conversationId
                        )
                    }
                    "conv_safir" -> {
                        repository.sendMessage(
                            content = "Salam Ahmad Shah. Message received. Please be ready with the child's immunization card and records at Nang Curative Hospital.",
                            senderRole = "DOCTOR",
                            senderName = "Dr. Safir Khan",
                            conversationId = conversationId
                        )
                    }
                    else -> {
                        val doc = doctors.value.firstOrNull { it.id == conversationId.removePrefix("conv_") }
                        val doctorName = doc?.name ?: "Attending Specialist"
                        repository.sendMessage(
                            content = "Salam Ahmad Shah. Your message has been received by $doctorName.",
                            senderRole = "DOCTOR",
                            senderName = doctorName,
                            conversationId = conversationId
                        )
                    }
                }
            }
        }
    }
}
