package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AppointmentStatus
import com.example.data.model.ClaimStatus
import com.example.data.model.VisitType

class RoomTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;")
    }

    @TypeConverter
    fun fromVisitType(type: VisitType?): String = type?.name ?: VisitType.IN_PERSON.name

    @TypeConverter
    fun toVisitType(value: String?): VisitType =
        try { VisitType.valueOf(value ?: VisitType.IN_PERSON.name) } catch (e: Exception) { VisitType.IN_PERSON }

    @TypeConverter
    fun fromAppointmentStatus(status: AppointmentStatus?): String = status?.name ?: AppointmentStatus.SUBMITTED.name

    @TypeConverter
    fun toAppointmentStatus(value: String?): AppointmentStatus =
        try { AppointmentStatus.valueOf(value ?: AppointmentStatus.SUBMITTED.name) } catch (e: Exception) { AppointmentStatus.SUBMITTED }

    @TypeConverter
    fun fromClaimStatus(status: ClaimStatus?): String = status?.name ?: ClaimStatus.SUBMITTED.name

    @TypeConverter
    fun toClaimStatus(value: String?): ClaimStatus =
        try { ClaimStatus.valueOf(value ?: ClaimStatus.SUBMITTED.name) } catch (e: Exception) { ClaimStatus.SUBMITTED }
}
