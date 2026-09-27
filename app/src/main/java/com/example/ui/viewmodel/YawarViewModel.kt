package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthApiException
import com.example.data.auth.AuthTokenStore
import com.example.data.auth.AssignDoctorRequest
import com.example.data.auth.CodeRequest
import com.example.data.auth.CredentialsRequest
import com.example.data.auth.DoctorInput
import com.example.data.auth.DoctorRequestUpdate
import com.example.data.auth.EmailRequest
import com.example.data.auth.HostingerAuthApi
import com.example.data.auth.HostingerWorkflowApi
import com.example.data.auth.HospitalInput
import com.example.data.auth.HospitalUserInput
import com.example.data.auth.NewCareRequest
import com.example.data.auth.PasswordResetRequest
import com.example.data.auth.PaymentInput
import com.example.data.auth.ReviewRequest
import com.example.data.auth.RouteRequest
import com.example.data.auth.SignupRequest
import com.example.data.auth.SendCaseMessage
import com.example.data.auth.WorkflowDoctor
import com.example.data.auth.WorkflowHospital
import com.example.data.auth.WorkflowMessage
import com.example.data.auth.WorkflowPayment
import com.example.data.auth.WorkflowRequest
import com.example.data.auth.requireSuccessfulBody
import com.example.data.auth.requireWorkflowSuccess
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    val appointmentDate: String = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
    ),
    val timeSlot: String = "10:00 AM",
    val corporateMemberId: String = "",
    val attachedDocs: List<String> = emptyList(),
    val contactPreference: String = "WhatsApp & In-App",
    val consentGiven: Boolean = false
)

class YawarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: YawarRepository
    private val authApi = HostingerAuthApi.create()
    private val workflowApi = HostingerWorkflowApi.create()
    private val authTokenStore = AuthTokenStore(application)
    private var workflowRefreshJob: Job? = null
    private var workflowErrorShown = false

    // Role & Language State
    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    private val _authNotice = MutableStateFlow<String?>(null)
    val authNotice: StateFlow<String?> = _authNotice.asStateFlow()
    private val _emailVerificationPending = MutableStateFlow(false)
    val emailVerificationPending: StateFlow<Boolean> = _emailVerificationPending.asStateFlow()
    private val _passwordResetPending = MutableStateFlow(false)
    val passwordResetPending: StateFlow<Boolean> = _passwordResetPending.asStateFlow()
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.PATIENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    init {
        val database = YawarDatabase.getDatabase(application)
        repository = YawarRepository(database.yawarDao())
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            val cleanup = application.getSharedPreferences("yawar_data_migration", android.content.Context.MODE_PRIVATE)
            if (!cleanup.getBoolean("legacy_demo_records_removed_v1", false)) {
                repository.clearAccountPrivateData()
                cleanup.edit().putBoolean("legacy_demo_records_removed_v1", true).apply()
            }
            restoreHostingerSession()
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) return setAuthError("Enter your email and password.")
        performAuth {
            val response = authApi.signIn(CredentialsRequest(normalizeEmail(email), password))
            acceptSession(response.requireSuccessfulBody())
        }
    }

    fun signUp(
        email: String,
        password: String,
        role: UserRole,
        fullName: String,
        phone: String,
        specialty: String,
        licenseNumber: String
    ) {
        if (email.isBlank() || !email.contains('@')) return setAuthError("Enter a valid email address.")
        if (password.length < 12) return setAuthError("Use a password with at least 12 characters.")
        if (role !in setOf(UserRole.PATIENT, UserRole.DOCTOR)) return setAuthError("Choose patient or doctor registration.")
        if (fullName.isBlank() || phone.isBlank()) return setAuthError("Enter your name and phone number.")
        if (role == UserRole.DOCTOR && (specialty.isBlank() || licenseNumber.isBlank())) {
            return setAuthError("Doctors must enter a specialty and license number.")
        }
        performAuth {
            val response = authApi.signUp(
                SignupRequest(
                    email = normalizeEmail(email),
                    password = password,
                    role = if (role == UserRole.DOCTOR) "doctor" else "patient",
                    full_name = fullName.trim(),
                    phone = phone.trim(),
                    specialty = specialty.trim(),
                    license_number = licenseNumber.trim()
                )
            )
            val result = response.requireSuccessfulBody()
            _emailVerificationPending.value = true
            _passwordResetPending.value = false
            _authNotice.value = result.message ?: "If the account can be registered, a six-digit verification code has been sent."
        }
    }

    fun verifySignUpCode(email: String, code: String) {
        if (code.length != 6 || code.any { !it.isDigit() }) return setAuthError("Enter the six-digit code from your email.")
        performAuth {
            val response = authApi.verifySignUp(CodeRequest(normalizeEmail(email), code))
            acceptSession(response.requireSuccessfulBody())
            _emailVerificationPending.value = false
        }
    }

    fun resendSignUpCode(email: String) {
        performAuth {
            val response = authApi.resendSignUpCode(EmailRequest(normalizeEmail(email)))
            val result = response.requireSuccessfulBody()
            _authNotice.value = result.message ?: "If the account is awaiting verification, a code has been sent."
            _emailVerificationPending.value = true
            _passwordResetPending.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !email.contains('@')) return setAuthError("Enter the email address for your account.")
        performAuth {
            val response = authApi.startPasswordReset(EmailRequest(normalizeEmail(email)))
            val result = response.requireSuccessfulBody()
            _emailVerificationPending.value = false
            _passwordResetPending.value = true
            _authNotice.value = result.message ?: "If an account exists for that address, a password reset code has been sent."
        }
    }

    fun finishPasswordReset(email: String, code: String, newPassword: String) {
        if (code.length != 6 || code.any { !it.isDigit() }) return setAuthError("Enter the six-digit code from your email.")
        if (newPassword.length < 12) return setAuthError("Use a new password with at least 12 characters.")
        performAuth {
            val response = authApi.resetPassword(
                PasswordResetRequest(normalizeEmail(email), code, newPassword)
            )
            val result = response.requireSuccessfulBody()
            _passwordResetPending.value = false
            _authNotice.value = result.message ?: "Your password has been changed. Sign in with the new password."
        }
    }

    fun cancelEmailCodeFlow() {
        _emailVerificationPending.value = false
        _passwordResetPending.value = false
        clearAuthFeedback()
    }

    private fun restoreHostingerSession() {
        val token = authTokenStore.read() ?: return
        _authLoading.value = true
        viewModelScope.launch {
            try {
                val response = authApi.currentUser("Bearer $token")
                val result = response.requireSuccessfulBody()
                applyUser(result.user)
                _authLoading.value = false
            } catch (error: Exception) {
                _authLoading.value = false
                if (error is CancellationException) throw error
                if (error is AuthApiException && error.statusCode in 400..403) {
                    authTokenStore.clear()
                } else {
                    setAuthError("Could not reconnect to your saved account. Sign in again when you have a connection.")
                }
            }
        }
    }

    private fun performAuth(action: suspend () -> Unit) {
        _authLoading.value = true
        clearAuthFeedback()
        viewModelScope.launch {
            try {
                action()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                val message = if (error is AuthApiException) error.message else
                    "Could not reach the sign-in service. Check your connection and try again."
                setAuthError(message ?: "The request could not be completed. Please try again.")
            } finally {
                _authLoading.value = false
            }
        }
    }

    private fun acceptSession(result: com.example.data.auth.AuthApiResult) {
        val token = result.token ?: throw IllegalStateException("The server did not return a session token.")
        authTokenStore.write(token)
        applyUser(result.user)
        clearAuthFeedback()
    }

    private fun applyUser(user: com.example.data.auth.AuthApiUser?) {
        val role = when (user?.role?.lowercase(Locale.ROOT)) {
            "doctor" -> UserRole.DOCTOR
            "call_center" -> UserRole.CALL_CENTER
            "hospital" -> UserRole.HOSPITAL
            "admin" -> UserRole.ADMIN
            else -> UserRole.PATIENT
        }
        _currentRole.value = role
        _isUserLoggedIn.value = user != null
        if (user != null) {
            _userProfile.value = UserProfileEntity(
                fullName = user.full_name,
                phone = user.phone,
                email = user.email
            )
            authTokenStore.read()?.let(::startWorkflowPolling)
        } else {
            workflowRefreshJob?.cancel()
        }
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    private fun setAuthError(message: String) { _authError.value = message }
    private fun clearAuthFeedback() { _authError.value = null; _authNotice.value = null }

    fun clearAuthError() { _authError.value = null }

    fun logout() {
        val token = authTokenStore.read()
        authTokenStore.clear()
        _isUserLoggedIn.value = false
        _currentRole.value = UserRole.PATIENT
        workflowRefreshJob?.cancel()
        _hospitalPayments.value = emptyList()
        _managedDoctors.value = emptyList()
        _appointments.value = emptyList()
        _messages.value = emptyList()
        _userProfile.value = null
        _selectedCareRequestId.value = ""
        _emailVerificationPending.value = false
        _passwordResetPending.value = false
        if (token != null) {
            viewModelScope.launch {
                runCatching { authApi.signOut("Bearer $token") }
            }
        }
        viewModelScope.launch(Dispatchers.IO) { repository.clearAccountPrivateData() }
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

    private val _appointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val appointments: StateFlow<List<AppointmentEntity>> = _appointments.asStateFlow()

    val claims: StateFlow<List<ClaimEntity>> = repository.allClaims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cases: StateFlow<List<CoordinationCaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()

    private val _hospitalPayments = MutableStateFlow<List<WorkflowPayment>>(emptyList())
    val hospitalPayments: StateFlow<List<WorkflowPayment>> = _hospitalPayments.asStateFlow()

    private val _managedDoctors = MutableStateFlow<List<WorkflowDoctor>>(emptyList())
    val managedDoctors: StateFlow<List<WorkflowDoctor>> = _managedDoctors.asStateFlow()

    private val _selectedCareRequestId = MutableStateFlow("")
    val selectedCareRequestId: StateFlow<String> = _selectedCareRequestId.asStateFlow()

    private val _workflowRefreshing = MutableStateFlow(false)
    val workflowRefreshing: StateFlow<Boolean> = _workflowRefreshing.asStateFlow()

    fun selectCareRequest(id: String) { _selectedCareRequestId.value = id }

    fun refreshWorkflow() {
        val token = authTokenStore.read() ?: return
        viewModelScope.launch {
            try {
                refreshWorkflowOnce(token)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showSnackbar(error.message ?: "Could not refresh shared care records.")
            }
        }
    }

    private fun startWorkflowPolling(token: String) {
        workflowRefreshJob?.cancel()
        workflowErrorShown = false
        workflowRefreshJob = viewModelScope.launch {
            while (isActive && _isUserLoggedIn.value && authTokenStore.read() == token) {
                try {
                    refreshWorkflowOnce(token)
                    workflowErrorShown = false
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    if (!workflowErrorShown) {
                        showSnackbar("Shared care service is unavailable. Your account is still signed in.")
                        workflowErrorShown = true
                    }
                }
                delay(30_000)
            }
        }
    }

    private suspend fun refreshWorkflowOnce(token: String) {
        _workflowRefreshing.value = true
        try {
            val dashboard = workflowApi.dashboard("Bearer $token").requireWorkflowSuccess().data
                ?: throw IllegalStateException("The care service returned no dashboard data.")
            val remoteRequests = dashboard.requests.map(::toAppointment)
            val remoteMessages = dashboard.messages.map(::toMessage)
            withContext(Dispatchers.IO) {
                repository.replaceHospitals(dashboard.hospitals.map(::toFacility))
                repository.replaceDoctors(dashboard.doctors.map(::toDoctor))
            }
            _appointments.value = remoteRequests
            _messages.value = remoteMessages
            dashboard.profile?.let { profile ->
                _userProfile.value = UserProfileEntity(
                    fullName = profile.fullName,
                    phone = profile.phone,
                    email = profile.email
                )
            }
            _hospitalPayments.value = dashboard.payments
            _managedDoctors.value = dashboard.doctors
        } finally {
            _workflowRefreshing.value = false
        }
    }

    private fun toAppointment(request: WorkflowRequest) = AppointmentEntity(
        id = request.id,
        patientName = request.patientName,
        patientPhone = request.patientPhone,
        specialty = request.specialty,
        reasonForCare = request.reasonForCare,
        symptomsSummary = request.symptomsSummary,
        urgency = request.urgency,
        visitType = runCatching { VisitType.valueOf(request.visitType) }.getOrDefault(VisitType.IN_PERSON),
        province = request.province,
        facilityId = request.facilityId,
        facilityName = request.facilityName,
        doctorId = request.doctorId,
        doctorName = request.doctorName,
        appointmentDate = request.appointmentDate,
        timeSlot = request.timeSlot,
        attachedDocuments = request.attachedDocuments,
        status = runCatching { AppointmentStatus.valueOf(request.status) }.getOrDefault(AppointmentStatus.SUBMITTED),
        coordinatorNotes = request.coordinatorNotes,
        createdAtTimestamp = request.createdAtTimestamp
    )

    private fun toMessage(message: WorkflowMessage) = MessageEntity(
        id = message.id,
        senderRole = message.senderRole,
        senderName = message.senderName,
        content = message.content,
        timestamp = message.timestamp,
        isRead = message.isRead,
        conversationId = message.conversationId,
        messageType = message.messageType,
        attachmentName = message.attachmentName,
        attachmentSize = message.attachmentSize,
        voiceDurationSec = message.voiceDurationSec,
        deliveryStatus = message.deliveryStatus
    )

    private fun toFacility(hospital: WorkflowHospital) = FacilityEntity(
        id = hospital.id,
        name = hospital.name,
        facilityType = hospital.facilityType,
        province = hospital.province,
        district = hospital.district,
        address = hospital.address,
        distanceKm = hospital.distanceKm,
        travelTimeMin = hospital.travelTimeMin,
        hasEmergency24h = hospital.hasEmergency24h,
        emergencyPhone = hospital.emergencyPhone,
        contactPhone = hospital.contactPhone,
        whatsappPhone = hospital.whatsappPhone,
        departments = hospital.departments,
        operatingHours = hospital.operatingHours,
        acceptedProgrammes = hospital.acceptedProgrammes,
        latitude = hospital.latitude,
        longitude = hospital.longitude,
        verificationStatus = hospital.verificationStatus,
        mapSearchUrl = hospital.mapSearchUrl,
        logoFile = hospital.logoFile
    )

    private fun toDoctor(doctor: WorkflowDoctor) = DoctorEntity(
        id = doctor.id,
        name = doctor.name,
        specialty = doctor.specialty,
        subspecialty = doctor.subspecialty,
        hospitalAffiliation = doctor.hospitalAffiliation,
        province = doctor.province,
        city = doctor.city,
        languages = doctor.languages,
        yearsExperience = doctor.yearsExperience,
        earliestAvailable = doctor.earliestAvailable,
        inPersonAvailable = doctor.inPersonAvailable,
        virtualAvailable = doctor.virtualAvailable,
        homeVisitAvailable = doctor.homeVisitAvailable,
        consultationFeeAf = doctor.consultationFeeAf,
        isVerified = doctor.isVerified,
        distanceKm = doctor.distanceKm,
        education = doctor.education,
        licenseNo = doctor.licenseNo,
        weeklySchedule = doctor.weeklySchedule,
        preparationNote = doctor.preparationNote,
        verificationStatus = doctor.verificationStatus,
        photoFile = doctor.photoFile,
        bio = doctor.bio
    )

    private fun workflowAction(action: suspend (String) -> Unit) {
        val token = authTokenStore.read()
        if (token == null) return showSnackbar("Sign in again to continue.")
        viewModelScope.launch {
            try {
                action(token)
                refreshWorkflowOnce(token)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showSnackbar(error.message ?: "The care service could not complete this action.")
            }
        }
    }

    fun reviewCareRequest(id: String, notes: String = "Reviewed by Yawar call center.") = workflowAction { token ->
        workflowApi.reviewRequest("Bearer $token", id, ReviewRequest(notes)).requireWorkflowSuccess()
    }

    fun routeCareRequest(id: String, hospitalId: String) {
        val parsedId = hospitalId.toLongOrNull() ?: return showSnackbar("That hospital is not available for referrals.")
        workflowAction { token -> workflowApi.routeRequest("Bearer $token", id, RouteRequest(parsedId)).requireWorkflowSuccess() }
    }

    fun assignDoctorToRequest(requestId: String, doctorId: String) {
        val parsedId = doctorId.toLongOrNull() ?: return showSnackbar("Choose a verified doctor.")
        workflowAction { token ->
            workflowApi.assignDoctor("Bearer $token", requestId, AssignDoctorRequest(parsedId)).requireWorkflowSuccess()
        }
    }

    fun saveHospitalPayout(input: PaymentInput) = workflowAction { token ->
        workflowApi.savePayment("Bearer $token", input).requireWorkflowSuccess()
    }

    fun addHospital(input: HospitalInput) = workflowAction { token ->
        workflowApi.createHospital("Bearer $token", input).requireWorkflowSuccess()
    }

    fun removeHospital(id: String) {
        val parsedId = id.toLongOrNull() ?: return
        workflowAction { token -> workflowApi.deactivateHospital("Bearer $token", parsedId).requireWorkflowSuccess() }
    }

    fun addHospitalAccount(hospitalId: String, email: String, name: String, phone: String) {
        val parsedId = hospitalId.toLongOrNull() ?: return showSnackbar("Choose a saved hospital first.")
        workflowAction { token ->
            workflowApi.createHospitalUser("Bearer $token", HospitalUserInput(parsedId, email.trim(), name.trim(), phone.trim())).requireWorkflowSuccess()
        }
    }

    fun addDoctor(email: String, name: String, phone: String, specialty: String, licenseNumber: String, hospitalId: String, photoUrl: String) {
        workflowAction { token ->
            workflowApi.createDoctor(
                "Bearer $token",
                DoctorInput(email.trim(), name.trim(), phone.trim(), specialty.trim(), licenseNumber.trim(), hospitalId.toLongOrNull(), photoUrl.trim())
            ).requireWorkflowSuccess()
        }
    }

    fun setDoctorVerified(id: String, verified: Boolean) {
        val parsedId = id.toLongOrNull() ?: return
        workflowAction { token ->
            workflowApi.verifyDoctor("Bearer $token", parsedId, mapOf("verified" to verified)).requireWorkflowSuccess()
        }
    }

    fun removeDoctor(id: String) {
        val parsedId = id.toLongOrNull() ?: return
        workflowAction { token -> workflowApi.deactivateDoctor("Bearer $token", parsedId).requireWorkflowSuccess() }
    }

    fun uploadHospitalDocument(requestId: String, resolver: ContentResolver, uri: Uri) {
        workflowAction { token ->
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"
            val fileName = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "hospital-record"
            val bytes = withContext(Dispatchers.IO) { resolver.openInputStream(uri)?.use { it.readBytes() } }
                ?: throw IllegalStateException("Could not open the selected file.")
            if (bytes.isEmpty() || bytes.size > 15 * 1024 * 1024) throw IllegalArgumentException("Choose a non-empty file smaller than 15 MB.")
            val body = bytes.toRequestBody(mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, body)
            workflowApi.uploadDocument("Bearer $token", requestId.toRequestBody("text/plain".toMediaType()), part).requireWorkflowSuccess()
        }
    }

    fun markHospitalDocumentsComplete(requestId: String) = workflowAction { token ->
        workflowApi.completeDocuments("Bearer $token", requestId).requireWorkflowSuccess()
    }

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
            doctorId = "",
            doctorName = "",
            facilityId = "",
            facilityName = "",
            isFirstSuitableDoctor = true
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
            if (draft.reasonForCare.isBlank()) {
                showSnackbar("Describe your health concern before sending the request.")
                return@launch
            }
            if (!draft.consentGiven) {
                showSnackbar("Review the privacy notice and provide your consent before sending this request.")
                return@launch
            }
            val token = authTokenStore.read()
            if (token == null) {
                showSnackbar("Sign in again to send a care request.")
                return@launch
            }
            try {
                val result = workflowApi.createRequest(
                    "Bearer $token",
                    NewCareRequest(
                        specialty = draft.specialty,
                        reasonForCare = draft.reasonForCare.trim(),
                        symptomsSummary = draft.symptomsSummary.trim(),
                        urgency = draft.urgency,
                        visitType = draft.visitType.name,
                        province = draft.province,
                        appointmentDate = draft.appointmentDate,
                        timeSlot = draft.timeSlot,
                        consentGiven = draft.consentGiven
                    )
                ).requireWorkflowSuccess()
                refreshWorkflowOnce(token)
                _lastSubmittedReference.value = result.data?.request?.id ?: appointments.value.firstOrNull()?.id
                _bookingStep.value = 9
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showSnackbar(error.message ?: "Could not send your request. Try again when you have a connection.")
            }
        }
    }

    // Doctor Actions
    fun doctorConfirmAppointment(id: String) {
        workflowAction { token ->
            workflowApi.updateDoctorRequest("Bearer $token", id, DoctorRequestUpdate("CONFIRMED", "Confirmed by the treating doctor.")).requireWorkflowSuccess()
        }
    }

    fun doctorDeclineAppointment(id: String, reason: String) {
        workflowAction { token ->
            workflowApi.updateDoctorRequest("Bearer $token", id, DoctorRequestUpdate("CANCELLED", "Doctor unavailable: $reason")).requireWorkflowSuccess()
        }
    }

    fun doctorCompleteVisit(id: String, summary: String) {
        workflowAction { token ->
            workflowApi.updateDoctorRequest("Bearer $token", id, DoctorRequestUpdate("COMPLETED", summary)).requireWorkflowSuccess()
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
            val profile = userProfile.value
            val claim = ClaimEntity(
                id = claimId,
                patientName = profile?.fullName.orEmpty(),
                claimType = claimType,
                providerOrHospital = provider,
                dateOfService = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                invoiceAmountAf = amount,
                approvedAmountAf = 0.0,
                status = ClaimStatus.SUBMITTED,
                documentTypes = listOf("Itemized Invoice", "Doctor Prescription"),
                remarks = notes.ifBlank { "Claim submitted via mobile app." },
                submissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            )
            repository.submitClaim(claim)
            showSnackbar("Claim $claimId saved on this device.")
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
            val token = authTokenStore.read()
            val requestId = conversationId.removePrefix("case_")
            if (token == null || requestId == conversationId) {
                showSnackbar("Open a care request to message its care team.")
                return@launch
            }
            try {
                workflowApi.sendMessage("Bearer $token", SendCaseMessage(requestId, content.trim()))
                    .requireWorkflowSuccess()
                refreshWorkflowOnce(token)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                showSnackbar(error.message ?: "The message could not be sent.")
            }
        }
    }
}
