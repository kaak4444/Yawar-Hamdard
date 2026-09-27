package com.example.ui.common

import com.example.data.model.AppLanguage

object AppStrings {
    fun getEmergencyBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Emergency Support: +93 707 438 303"
        AppLanguage.DARI -> "پشتیبانی عاجل یاور: ۷۰۷۴۳۸۳۰۳ ۹۳+"
        AppLanguage.PASHTO -> "د یاور عاجلې مرستې: ۷۰۷۴۳۸۳۰۳ ۹۳+"
    }

    fun getEmergencySubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Call YHCS Emergency Support: +93 707 438 303. Users with an immediate life-threatening emergency should also seek the nearest capable emergency service."
        AppLanguage.DARI -> "تماس با پشتیبانی عاجل یاور: ۷۰۷۴۳۸۳۰۳ ۹۳+. مراجعین با حالات اضطراری شدید باید همزمان به نزدیک‌ترین بخش عاجل مراجعه نمایند."
        AppLanguage.PASHTO -> "د یاور بېړنی ملاتړ: ۷۰۷۴۳۸۳۰۳ ۹۳+. هغه مراجعین چې سمدستي ګواښ سره مخ دي باید نږدې روغتون ته هم مراجعه وکړي."
    }

    fun getHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Home"
        AppLanguage.DARI -> "صفحه اصلی"
        AppLanguage.PASHTO -> "اصلي مخ"
    }

    fun getFindCare(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Doctors & Hospitals"
        AppLanguage.DARI -> "داکتران و شفاخانه‌ها"
        AppLanguage.PASHTO -> "ډاکټران او روغتونونه"
    }

    fun getAppointments(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Appointments"
        AppLanguage.DARI -> "نوبت‌ها"
        AppLanguage.PASHTO -> "وختونه"
    }

    fun getMessages(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Messages"
        AppLanguage.DARI -> "پیام‌ها"
        AppLanguage.PASHTO -> "پیغامونه"
    }

    fun getProfile(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Profile"
        AppLanguage.DARI -> "حساب من"
        AppLanguage.PASHTO -> "پروفایل"
    }

    fun getDashboard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Dashboard"
        AppLanguage.DARI -> "داشبورد"
        AppLanguage.PASHTO -> "ډشبورډ"
    }

    fun getSchedule(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Schedule"
        AppLanguage.DARI -> "تقویم کاری"
        AppLanguage.PASHTO -> "مهالوېش"
    }

    fun getPatients(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Patients"
        AppLanguage.DARI -> "بیماران"
        AppLanguage.PASHTO -> "ناروغان"
    }

    fun getQueue(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Queue"
        AppLanguage.DARI -> "صف هماهنگی"
        AppLanguage.PASHTO -> "د همغږۍ قطار"
    }

    fun getBookAppointment(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Request Appointment"
        AppLanguage.DARI -> "درخواست نوبت"
        AppLanguage.PASHTO -> "د نوبت غوښتنه"
    }

    fun getFindDoctor(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Find Doctor"
        AppLanguage.DARI -> "جستجوی داکتر"
        AppLanguage.PASHTO -> "ډاکټر موندل"
    }

    fun getFindHospital(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Find Hospital"
        AppLanguage.DARI -> "شفاخانه‌ها"
        AppLanguage.PASHTO -> "روغتونونه"
    }

    fun getArrangeCare(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Arrange Care"
        AppLanguage.DARI -> "هماهنگی تداوی"
        AppLanguage.PASHTO -> "د درملنې همغږي"
    }

    fun getUploadReferral(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Upload Referral"
        AppLanguage.DARI -> "ارسال ورقه ارجاع"
        AppLanguage.PASHTO -> "د راجع کاغذ استول"
    }

    fun getClaimsAndCashless(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Claims & Direct Billing"
        AppLanguage.DARI -> "دعاوی و تداوی بدون پول"
        AppLanguage.PASHTO -> "ادعاوې او بې‌پیسو درملنه"
    }

    fun getCoordinationServices(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Care Coordination"
        AppLanguage.DARI -> "هماهنگی مراقبت صحی"
        AppLanguage.PASHTO -> "د روغتیایی څارنې همغږي"
    }

    fun getCoordinationCases(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Care Coordination"
        AppLanguage.DARI -> "دوسیه‌های هماهنگی"
        AppLanguage.PASHTO -> "د همغږۍ دوسیې"
    }

    fun getSearchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Search verified doctor, specialty, hospital..."
        AppLanguage.DARI -> "جستجوی داکتر، تخصص، شفاخانه..."
        AppLanguage.PASHTO -> "د ډاکټر، تخصص یا روغتون لټون..."
    }

    fun getUpcomingAppointment(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Upcoming Appointment"
        AppLanguage.DARI -> "نوبت بعدی شما"
        AppLanguage.PASHTO -> "ستاسو راتلونکی وخت"
    }

    fun getServicesTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Yawar Healthcare Services"
        AppLanguage.DARI -> "خدمات صحی و تخصصی یاور"
        AppLanguage.PASHTO -> "د یاور روغتیایی خدمتونه"
    }

    fun getNearbyHospitals(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Verified Hospitals & Clinics"
        AppLanguage.DARI -> "شفاخانه‌ها و مراکز صحی معتبر"
        AppLanguage.PASHTO -> "تایید شوي روغتونونه او کلینیکونه"
    }

    fun getNearbyDoctors(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Verified Specialists"
        AppLanguage.DARI -> "داکتران متخصص تأیید شده"
        AppLanguage.PASHTO -> "تایید شوي متخصصین"
    }

    fun getCallCenterAlertNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> "Your care request is waiting for Yawar call-center review"
        AppLanguage.DARI -> "درخواست مراقبت شما در انتظار بررسی مرکز تماس یاور است"
        AppLanguage.PASHTO -> "ستاسو د پاملرنې غوښتنه د یاور د اړیکو مرکز د ارزونې په تمه ده"
    }
}
