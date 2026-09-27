package com.example.data.auth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal data class WorkflowApiResponse(
    val ok: Boolean = false,
    val data: WorkflowDashboard? = null,
    val error: String? = null
)

internal data class WorkflowDashboard(
    val request: WorkflowRequest? = null,
    val requests: List<WorkflowRequest> = emptyList(),
    val messages: List<WorkflowMessage> = emptyList(),
    val hospitals: List<WorkflowHospital> = emptyList(),
    val doctors: List<WorkflowDoctor> = emptyList(),
    val payments: List<WorkflowPayment> = emptyList(),
    val profile: WorkflowProfile? = null
)

internal data class WorkflowRequest(
    val id: String,
    val patientName: String = "",
    val patientPhone: String = "",
    val specialty: String = "General consultation",
    val reasonForCare: String = "",
    val symptomsSummary: String = "",
    val urgency: String = "Routine",
    val visitType: String = "IN_PERSON",
    val province: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val appointmentDate: String = "",
    val timeSlot: String = "",
    val attachedDocuments: List<String> = emptyList(),
    val uploadedDocumentIds: List<String> = emptyList(),
    val status: String = "SUBMITTED",
    val coordinatorNotes: String = "",
    val createdAtTimestamp: Long = 0L
)

internal data class WorkflowMessage(
    val id: String,
    val senderRole: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val conversationId: String = "",
    val messageType: String = "TEXT",
    val attachmentName: String = "",
    val attachmentSize: String = "",
    val voiceDurationSec: Int = 0,
    val deliveryStatus: String = "SENT"
)

internal data class WorkflowHospital(
    val id: String,
    val name: String,
    val facilityType: String = "Partner Hospital",
    val province: String = "",
    val district: String = "",
    val address: String = "",
    val distanceKm: Double = 0.0,
    val travelTimeMin: Int = 0,
    val hasEmergency24h: Boolean = false,
    val emergencyPhone: String = "",
    val contactPhone: String = "",
    val departments: List<String> = emptyList(),
    val operatingHours: String = "",
    val acceptedProgrammes: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val verificationStatus: String = "Partner Hospital",
    val mapSearchUrl: String = "",
    val logoFile: String = "",
    val email: String = "",
    val whatsappPhone: String = ""
)

internal data class WorkflowDoctor(
    val id: String,
    val name: String,
    val specialty: String,
    val subspecialty: String = "",
    val hospitalAffiliation: String = "",
    val province: String = "",
    val city: String = "",
    val languages: List<String> = emptyList(),
    val yearsExperience: Int = 0,
    val earliestAvailable: String = "",
    val inPersonAvailable: Boolean = true,
    val virtualAvailable: Boolean = false,
    val homeVisitAvailable: Boolean = false,
    val consultationFeeAf: String = "",
    val isVerified: Boolean = false,
    val distanceKm: Double = 0.0,
    val education: String = "",
    val licenseNo: String = "",
    val weeklySchedule: String = "",
    val preparationNote: String = "",
    val verificationStatus: String = "Pending Verification",
    val photoFile: String = "",
    val bio: String = "",
    val phone: String = ""
)

internal data class WorkflowPayment(
    val id: String,
    val hospitalId: String,
    val hospitalName: String,
    val month: String,
    val amountAf: Double,
    val dueDate: String,
    val paidAt: String = "",
    val status: String = "SCHEDULED",
    val notes: String = ""
)

internal data class WorkflowProfile(
    val id: String = "current_user",
    val fullName: String = "",
    val phone: String = "",
    val email: String = ""
)

internal data class NewCareRequest(
    val specialty: String,
    val reasonForCare: String,
    val symptomsSummary: String,
    val urgency: String,
    val visitType: String,
    val province: String,
    val appointmentDate: String,
    val timeSlot: String,
    val consentGiven: Boolean
)

internal data class ReviewRequest(val notes: String)
internal data class DoctorRequestUpdate(val status: String, val notes: String = "")
internal data class RouteRequest(val hospitalId: Long)
internal data class AssignDoctorRequest(val doctorId: Long)
internal data class SendCaseMessage(val requestId: String, val content: String)
internal data class HospitalInput(
    val name: String,
    val email: String = "",
    val phone: String = "",
    val whatsappPhone: String = "",
    val province: String = "",
    val city: String = "",
    val address: String = "",
    val logoUrl: String = ""
)
internal data class HospitalUserInput(val hospitalId: Long, val email: String, val fullName: String, val phone: String)
internal data class DoctorInput(
    val email: String,
    val fullName: String,
    val phone: String,
    val specialty: String,
    val licenseNumber: String,
    val hospitalId: Long? = null,
    val photoUrl: String = ""
)
internal data class PaymentInput(
    val hospitalId: Long,
    val month: String,
    val amountAf: Double,
    val dueDate: String,
    val status: String,
    val notes: String = ""
)

internal interface HostingerWorkflowApi {
    @GET("dashboard")
    suspend fun dashboard(@Header("Authorization") authorization: String): Response<WorkflowApiResponse>

    @POST("requests")
    suspend fun createRequest(@Header("Authorization") authorization: String, @Body body: NewCareRequest): Response<WorkflowApiResponse>

    @POST("requests/{requestId}/review")
    suspend fun reviewRequest(@Header("Authorization") authorization: String, @Path("requestId") requestId: String, @Body body: ReviewRequest): Response<WorkflowApiResponse>

    @POST("requests/{requestId}/doctor-status")
    suspend fun updateDoctorRequest(@Header("Authorization") authorization: String, @Path("requestId") requestId: String, @Body body: DoctorRequestUpdate): Response<WorkflowApiResponse>

    @POST("requests/{requestId}/assign-doctor")
    suspend fun assignDoctor(@Header("Authorization") authorization: String, @Path("requestId") requestId: String, @Body body: AssignDoctorRequest): Response<WorkflowApiResponse>

    @POST("requests/{requestId}/route")
    suspend fun routeRequest(@Header("Authorization") authorization: String, @Path("requestId") requestId: String, @Body body: RouteRequest): Response<WorkflowApiResponse>

    @POST("messages")
    suspend fun sendMessage(@Header("Authorization") authorization: String, @Body body: SendCaseMessage): Response<WorkflowApiResponse>

    @Multipart
    @POST("documents")
    suspend fun uploadDocument(
        @Header("Authorization") authorization: String,
        @Part("requestId") requestId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<WorkflowApiResponse>

    @POST("requests/{requestId}/documents-complete")
    suspend fun completeDocuments(@Header("Authorization") authorization: String, @Path("requestId") requestId: String): Response<WorkflowApiResponse>

    @POST("payments")
    suspend fun savePayment(@Header("Authorization") authorization: String, @Body body: PaymentInput): Response<WorkflowApiResponse>

    @POST("hospitals")
    suspend fun createHospital(@Header("Authorization") authorization: String, @Body body: HospitalInput): Response<WorkflowApiResponse>

    @DELETE("hospitals/{hospitalId}")
    suspend fun deactivateHospital(@Header("Authorization") authorization: String, @Path("hospitalId") hospitalId: Long): Response<WorkflowApiResponse>

    @POST("hospital-users")
    suspend fun createHospitalUser(@Header("Authorization") authorization: String, @Body body: HospitalUserInput): Response<WorkflowApiResponse>

    @POST("doctors")
    suspend fun createDoctor(@Header("Authorization") authorization: String, @Body body: DoctorInput): Response<WorkflowApiResponse>

    @PUT("doctors/{doctorId}/verification")
    suspend fun verifyDoctor(@Header("Authorization") authorization: String, @Path("doctorId") doctorId: Long, @Body body: Map<String, Boolean>): Response<WorkflowApiResponse>

    @DELETE("doctors/{doctorId}")
    suspend fun deactivateDoctor(@Header("Authorization") authorization: String, @Path("doctorId") doctorId: Long): Response<WorkflowApiResponse>

    companion object {
        fun create(): HostingerWorkflowApi {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .callTimeout(45, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://yawarconsulting.com/wp-json/yh/v1/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(HostingerWorkflowApi::class.java)
        }
    }
}

internal fun Response<WorkflowApiResponse>.requireWorkflowSuccess(): WorkflowApiResponse {
    val response = body()
    if (!isSuccessful) {
        val raw = errorBody()?.use { it.string() }.orEmpty()
        val message = response?.error ?: runCatching {
            JSONObject(raw).optString("message").takeIf(String::isNotBlank)
                ?: JSONObject(raw).optString("error").takeIf(String::isNotBlank)
        }.getOrNull() ?: "The care service could not complete this action."
        throw AuthApiException(code(), message)
    }
    if (response == null || !response.ok) throw AuthApiException(code(), response?.error ?: "The care service returned an invalid response.")
    return response
}
