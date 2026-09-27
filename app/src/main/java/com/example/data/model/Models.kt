package com.example.data.model

enum class UserRole(val displayName: String, val titleFa: String, val titlePs: String) {
    PATIENT("Patient", "بیمار / مراجع", "ناروغ / مراجع"),
    DOCTOR("Doctor", "داکتر معالج", "ډاکټر"),
    ADMIN("YHCS Coordinator", "هماهنگ‌کننده یاور", "د یاور همغږی کوونکی")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", "English", false),
    DARI("prs", "Dari", "دری", true),
    PASHTO("ps", "Pashto", "پښتو", true)
}

enum class VisitType(val labelEn: String, val labelFa: String, val labelPs: String) {
    IN_PERSON("In-Person Visit", "معاینه حضوری", "حضوري لیدنه"),
    VIRTUAL("Virtual Consultation", "ویزیت آنلاین", "آنلاین مشوره"),
    HOME_VISIT("Home Care Visit", "ویزیت در منزل", "په کور کې لیدنه")
}

enum class AppointmentStatus(val labelEn: String, val labelFa: String, val labelPs: String) {
    SUBMITTED("Submitted", "ارسال شده", "سپارل شوی"),
    UNDER_REVIEW("Under Review", "در حال بررسی", "تر څیړنې لاندې"),
    AWAITING_PROVIDER("Awaiting Provider", "در انتظار داکتر", "د ډاکټر په تمه"),
    CONFIRMED("Confirmed", "تأیید شده", "تایید شوی"),
    CHECKED_IN("Checked In", "پذیرش شده", "ننوتی"),
    IN_CONSULTATION("In Consultation", "در حال معاینه", "د معاینې پر مهال"),
    COMPLETED("Completed", "تکمیل شده", "بشپړ شوی"),
    CANCELLED("Cancelled", "لغو شده", "لغوه شوی"),
    RESCHEDULE_REQUESTED("Reschedule Requested", "درخواست تغییر زمان", "د وخت بدلولو غوښتنه")
}

enum class ClaimStatus(val labelEn: String, val labelFa: String, val labelPs: String) {
    DRAFT("Draft", "پیش‌نویس", "مسوده"),
    SUBMITTED("Submitted", "ثبت شده", "سپارل شوی"),
    COMPLETENESS_REVIEW("Completeness Review", "بررسی اسناد", "د اسنادو بیاکتنه"),
    CLINICAL_REVIEW("Clinical Review", "بررسی بالینی", "کلینیکي بیاکتنه"),
    APPROVED("Approved", "تأیید شده", "تایید شوی"),
    PARTIALLY_APPROVED("Partially Approved", "تأیید جزئی", "قسمي تایید شوی"),
    DECLINED("Declined", "رد شده", "رد شوی"),
    PAID("Paid / Reimbursed", "پرداخت شده", "تادیه شوی"),
    CLOSED("Closed", "بسته شده", "تړل شوی")
}

enum class CaseType(val labelEn: String) {
    MEDICAL_COORDINATION("Medical Coordination"),
    EVACUATION_REFERRAL("Evacuation & Cross-Border Referral"),
    HEALTH_RISK_ASSESSMENT("Health-Risk Assessment"),
    CORPORATE_CARE("Expatriate & Corporate Care")
}

enum class CaseStatus(val labelEn: String) {
    INTAKE("Intake & Triage"),
    IN_PROGRESS("Active Coordination"),
    AWAITING_ACCEPTANCE("Awaiting Receiving Facility"),
    IN_TRANSIT("Transport / Transfer"),
    COMPLETED("Care Handover Completed"),
    FOLLOW_UP("Post-Care Follow-up")
}

data class ServiceItem(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val titlePs: String,
    val oneSentencePromise: String,
    val description: String,
    val forWhom: String,
    val whatWeCoordinate: String,
    val pathwaySteps: List<String>,
    val requiredDocuments: List<String>,
    val iconName: String
)
