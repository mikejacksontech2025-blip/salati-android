package com.example.prayer

enum class PrayerType(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val isMainPrayer: Boolean
) {
    FAJR("fajr", "الفجر", "Fajr", true),
    SUNRISE("sunrise", "الشروق", "Sunrise", false),
    DHUHR("dhuhr", "الظهر", "Dhuhr", true),
    ASR("asr", "العصر", "Asr", true),
    MAGHRIB("maghrib", "المغرب", "Maghrib", true),
    ISHA("isha", "العشاء", "Isha", true);

    companion object {
        fun fromId(id: String): PrayerType = values().find { it.id == id } ?: FAJR
        val mainPrayers = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
        val allSchedule = listOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA)
    }
}

enum class CalculationMethod(
    val titleArabic: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0
) {
    UMM_AL_QURA("أم القرى (مكة المكرمة)", 18.5, 0.0, 90),
    MUSLIM_WORLD_LEAGUE("رابطة العالم الإسلامي", 18.0, 17.0, 0),
    EGYPTIAN("الهيئة المصرية العامة للمساحة", 19.5, 17.5, 0),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0, 0),
    ISNA("الجمعية الإسلامية لأمريكا الشمالية", 15.0, 15.0, 0),
    KUWAIT("وزارة الأوقاف والشؤون الإسلامية بالكويت", 18.0, 17.5, 0)
}

enum class JuristicMethod(val titleArabic: String, val shadowFactor: Double) {
    SHAFI("جمهور الفقهاء (الشافعي، المالكي، الحنبلي)", 1.0),
    HANAFI("المذهب الحنفي (مثل الظل مرتين)", 2.0)
}

data class PrayerTimeItem(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime12h: String,
    val isPassed: Boolean = false,
    val isNext: Boolean = false,
    val isAdhanEnabled: Boolean = true
)

data class DayPrayerSchedule(
    val dateFormattedGregorian: String,
    val dateFormattedHijri: String,
    val prayers: List<PrayerTimeItem>,
    val nextPrayer: PrayerTimeItem?,
    val remainingMillisToNext: Long
)

data class CityLocation(
    val nameArabic: String,
    val countryArabic: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)
