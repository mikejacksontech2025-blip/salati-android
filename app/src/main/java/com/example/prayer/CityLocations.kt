package com.example.prayer

object CityLocations {
    val defaultCity = CityLocation("مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262, "Asia/Riyadh")

    val predefinedCities: List<CityLocation> = listOf(
        CityLocation("مكة المكرمة", "السعودية", 21.4225, 39.8262, "Asia/Riyadh"),
        CityLocation("المدينة المنورة", "السعودية", 24.4672, 39.6111, "Asia/Riyadh"),
        CityLocation("الرياض", "السعودية", 24.7136, 46.6753, "Asia/Riyadh"),
        CityLocation("جدة", "السعودية", 21.4858, 39.1925, "Asia/Riyadh"),
        CityLocation("القدس الشريف", "فلسطين", 31.7683, 35.2137, "Asia/Jerusalem"),
        CityLocation("القاهرة", "مصر", 30.0444, 31.2357, "Africa/Cairo"),
        CityLocation("الإسكندرية", "مصر", 31.2001, 29.9187, "Africa/Cairo"),
        CityLocation("عَمّان", "الأردن", 31.9454, 35.9284, "Asia/Amman"),
        CityLocation("دمشق", "سوريا", 33.5138, 36.2765, "Asia/Damascus"),
        CityLocation("بيروت", "لبنان", 33.8938, 35.5018, "Asia/Beirut"),
        CityLocation("بغداد", "العراق", 33.3152, 44.3661, "Asia/Baghdad"),
        CityLocation("الكويت", "الكويت", 29.3759, 47.9774, "Asia/Kuwait"),
        CityLocation("الدوحة", "قطر", 25.2854, 51.5310, "Asia/Qatar"),
        CityLocation("أبوظبي", "الإمارات", 24.4539, 54.3773, "Asia/Dubai"),
        CityLocation("دبي", "الإمارات", 25.2048, 55.2708, "Asia/Dubai"),
        CityLocation("المنامة", "البحرين", 26.2285, 50.5860, "Asia/Bahrain"),
        CityLocation("مسقط", "عُمان", 23.5880, 58.3829, "Asia/Muscat"),
        CityLocation("صنعاء", "اليمن", 15.3694, 44.1910, "Asia/Aden"),
        CityLocation("طرابلس", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli"),
        CityLocation("تونس", "تونس", 36.8065, 10.1815, "Africa/Tunis"),
        CityLocation("الجزائر", "الجزائر", 36.7538, 3.0588, "Africa/Algiers"),
        CityLocation("الرباط", "المغرب", 34.0209, -6.8416, "Africa/Casablanca"),
        CityLocation("الدار البيضاء", "المغرب", 33.5731, -7.5898, "Africa/Casablanca"),
        CityLocation("الخرطوم", "السودان", 15.5007, 32.5599, "Africa/Khartoum"),
        CityLocation("إسطنبول", "تركيا", 41.0082, 28.9784, "Europe/Istanbul")
    )
}
