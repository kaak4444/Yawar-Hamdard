package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.data.model.VisitType

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialty: String,
    val subspecialty: String,
    val hospitalAffiliation: String,
    val province: String,
    val city: String,
    val languages: List<String>,
    val yearsExperience: Int,
    val earliestAvailable: String,
    val inPersonAvailable: Boolean = true,
    val virtualAvailable: Boolean = false,
    val homeVisitAvailable: Boolean = false,
    val consultationFeeAf: String = "1,000 AFN",
    val isVerified: Boolean = true,
    val distanceKm: Double = 0.0,
    val education: String = "MD, Medical Faculty",
    val licenseNo: String = "MoPH-AF-2018-9410",
    val weeklySchedule: String = "Sat - Thu: 09:00 - 15:00",
    val preparationNote: String = "Please bring prior test results, imaging films, and active prescriptions.",
    val verificationStatus: String = "Verified Specialist",
    val photoFile: String = "",
    val bio: String = ""
)

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val facilityType: String,
    val province: String,
    val district: String,
    val address: String,
    val distanceKm: Double,
    val travelTimeMin: Int,
    val hasEmergency24h: Boolean,
    val emergencyPhone: String,
    val contactPhone: String,
    val departments: List<String>,
    val operatingHours: String,
    val acceptedProgrammes: List<String>,
    val latitude: Double,
    val longitude: Double,
    val verificationStatus: String = "Verified Facility",
    val mapSearchUrl: String = "",
    val logoFile: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientName: String,
    val patientPhone: String,
    val isDependent: Boolean = false,
    val dependentName: String = "",
    val dependentRelation: String = "",
    val specialty: String,
    val reasonForCare: String,
    val symptomsSummary: String = "",
    val urgency: String = "Routine", // Routine, Priority, Urgent
    val visitType: VisitType = VisitType.IN_PERSON,
    val province: String = "Kabul",
    val facilityId: String = "",
    val facilityName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val appointmentDate: String,
    val timeSlot: String,
    val corporateMemberId: String = "",
    val attachedDocuments: List<String> = emptyList(),
    val contactPreference: String = "WhatsApp & In-App",
    val status: AppointmentStatus = AppointmentStatus.SUBMITTED,
    val coordinatorNotes: String = "",
    val doctorPreparationNotes: String = "Bring existing medical reports",
    val emailAlertSentTo: String = "callcenter@yawarconsulting.com",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey val id: String,
    val patientName: String,
    val claimType: String, // "Direct Billing (Cashless)" or "Reimbursement"
    val providerOrHospital: String,
    val dateOfService: String,
    val invoiceAmountAf: Double,
    val approvedAmountAf: Double,
    val status: ClaimStatus,
    val documentTypes: List<String>,
    val remarks: String,
    val submissionDate: String
)

@Entity(tableName = "coordination_cases")
data class CoordinationCaseEntity(
    @PrimaryKey val id: String,
    val patientName: String,
    val caseType: String,
    val originCity: String,
    val destinationFacility: String,
    val currentStatus: String,
    val assignedCoordinator: String,
    val timelineNotes: List<String>,
    val lastUpdated: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderRole: String, // PATIENT, DOCTOR, COORDINATOR
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true,
    val conversationId: String = "conv_coord",
    val messageType: String = "TEXT", // TEXT, VOICE, DOCUMENT, IMAGE
    val attachmentName: String = "",
    val attachmentSize: String = "",
    val voiceDurationSec: Int = 0,
    val deliveryStatus: String = "READ" // SENDING, SENT, DELIVERED, READ
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val fullName: String = "Ahmad Shah",
    val phone: String = "+93 70 123 4567",
    val email: String = "ahmad.shah@example.af",
    val province: String = "Kabul",
    val district: String = "District 10, Shahr-e-Naw",
    val preferredLanguage: String = "en",
    val emergencyContact: String = "+93 79 988 7766 (Brother)",
    val organizationOrMemberId: String = "YHCS-CORP-9021",
    val lowBandwidthMode: Boolean = false
)
