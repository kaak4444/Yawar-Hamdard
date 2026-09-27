package com.example.data.repository

import com.example.data.local.AppointmentEntity
import com.example.data.local.ClaimEntity
import com.example.data.local.CoordinationCaseEntity
import com.example.data.local.DoctorEntity
import com.example.data.local.FacilityEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.YawarDao
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.data.seed.DataSeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class YawarRepository(private val dao: YawarDao) {

    val allDoctors: Flow<List<DoctorEntity>> = dao.getAllDoctors()
    val allFacilities: Flow<List<FacilityEntity>> = dao.getAllFacilities()
    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    val allClaims: Flow<List<ClaimEntity>> = dao.getAllClaims()
    val allCases: Flow<List<CoordinationCaseEntity>> = dao.getAllCases()
    val allMessages: Flow<List<MessageEntity>> = dao.getAllMessages()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun checkAndSeedDatabase() {
        val currentDoctors = dao.getAllDoctors().firstOrNull()
        if (currentDoctors.isNullOrEmpty()) {
            dao.insertDoctors(DataSeed.sampleDoctors)
            dao.insertFacilities(DataSeed.sampleFacilities)
        }
    }

    suspend fun clearAccountPrivateData() {
        dao.clearAppointments()
        dao.clearClaims()
        dao.clearCases()
        dao.clearMessages()
        dao.clearUserProfile()
    }

    suspend fun replaceCareRequests(requests: List<AppointmentEntity>) {
        dao.clearAppointments()
        if (requests.isNotEmpty()) dao.insertAppointments(requests)
    }

    suspend fun replaceCareMessages(messages: List<MessageEntity>) {
        dao.clearMessages()
        if (messages.isNotEmpty()) dao.insertMessages(messages)
    }

    suspend fun replaceHospitals(hospitals: List<FacilityEntity>) {
        dao.clearFacilities()
        if (hospitals.isNotEmpty()) dao.insertFacilities(hospitals)
    }

    suspend fun replaceDoctors(doctors: List<DoctorEntity>) {
        dao.clearDoctors()
        val merged = (doctors + DataSeed.sampleDoctors).distinctBy { it.name.trim().lowercase() }
        if (merged.isNotEmpty()) dao.insertDoctors(merged)
    }

    fun getDoctorById(id: String): Flow<DoctorEntity?> = dao.getDoctorById(id)
    fun getFacilityById(id: String): Flow<FacilityEntity?> = dao.getFacilityById(id)
    fun getAppointmentById(id: String): Flow<AppointmentEntity?> = dao.getAppointmentById(id)
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>> = dao.getAppointmentsForDoctor(doctorId)

    suspend fun submitAppointment(appointment: AppointmentEntity) {
        dao.insertAppointment(appointment)
    }

    suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus, notes: String = "") {
        dao.updateAppointmentStatus(id, status, notes)
    }

    suspend fun rescheduleAppointment(id: String, newDate: String, newSlot: String) {
        dao.rescheduleAppointment(id, newDate, newSlot, AppointmentStatus.RESCHEDULE_REQUESTED)
    }

    suspend fun submitClaim(claim: ClaimEntity) {
        dao.insertClaim(claim)
    }

    suspend fun updateClaimStatus(id: String, status: ClaimStatus) {
        dao.updateClaimStatus(id, status)
    }

    suspend fun submitCase(caseEntity: CoordinationCaseEntity) {
        dao.insertCase(caseEntity)
    }

    suspend fun sendMessage(
        content: String,
        senderRole: String,
        senderName: String,
        conversationId: String = "conv_coord",
        messageType: String = "TEXT",
        attachmentName: String = "",
        attachmentSize: String = "",
        voiceDurationSec: Int = 0,
        deliveryStatus: String = "READ"
    ) {
        val msg = MessageEntity(
            id = "msg_${System.currentTimeMillis()}",
            senderRole = senderRole,
            senderName = senderName,
            content = content,
            timestamp = System.currentTimeMillis(),
            conversationId = conversationId,
            messageType = messageType,
            attachmentName = attachmentName,
            attachmentSize = attachmentSize,
            voiceDurationSec = voiceDurationSec,
            deliveryStatus = deliveryStatus
        )
        dao.insertMessage(msg)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        dao.insertUserProfile(profile)
    }

    suspend fun updateDoctor(doctor: DoctorEntity) { dao.updateDoctor(doctor) }
}
