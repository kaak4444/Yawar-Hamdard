package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface YawarDao {
    // Doctors
    @Query("SELECT * FROM doctors ORDER BY isVerified DESC, yearsExperience DESC")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    fun getDoctorById(id: String): Flow<DoctorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)

    // Facilities
    @Query("SELECT * FROM facilities ORDER BY distanceKm ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE id = :id")
    fun getFacilityById(id: String): Flow<FacilityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<FacilityEntity>)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY createdAtTimestamp DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getAppointmentById(id: String): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY createdAtTimestamp DESC")
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Query("UPDATE appointments SET status = :status, coordinatorNotes = :notes WHERE id = :id")
    suspend fun updateAppointmentStatus(id: String, status: AppointmentStatus, notes: String)

    @Query("UPDATE appointments SET appointmentDate = :date, timeSlot = :slot, status = :status WHERE id = :id")
    suspend fun rescheduleAppointment(id: String, date: String, slot: String, status: AppointmentStatus)

    // Claims
    @Query("SELECT * FROM claims ORDER BY submissionDate DESC")
    fun getAllClaims(): Flow<List<ClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<ClaimEntity>)

    @Query("UPDATE claims SET status = :status WHERE id = :id")
    suspend fun updateClaimStatus(id: String, status: ClaimStatus)

    // Cases
    @Query("SELECT * FROM coordination_cases ORDER BY lastUpdated DESC")
    fun getAllCases(): Flow<List<CoordinationCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(coordinationCase: CoordinationCaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<CoordinationCaseEntity>)

    // Messages
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesByConversation(convId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
}
