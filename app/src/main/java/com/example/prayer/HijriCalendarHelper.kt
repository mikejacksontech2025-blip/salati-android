package com.example.prayer

import java.util.Calendar

object HijriCalendarHelper {

    private val hijriMonthNames = listOf(
        "محرّم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوّال",
        "ذو القعدة",
        "ذو الحجة"
    )

    private val dayOfWeekNames = listOf(
        "الأحد",
        "الإثنين",
        "الثلاثاء",
        "الأربعاء",
        "الخميس",
        "الجمعة",
        "السبت"
    )

    fun getDayOfWeekArabic(calendar: Calendar): String {
        val dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
        return if (dayIndex in dayOfWeekNames.indices) dayOfWeekNames[dayIndex] else ""
    }

    /**
     * Converts a Gregorian Calendar to Hijri Date (Year, Month 1-12, Day).
     * Uses the standard Kuwaiti / Astronomical Julian Day algorithm.
     */
    fun toHijri(calendar: Calendar, dayAdjustment: Int = 0): Triple<Int, Int, Int> {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        var a = y / 100
        var b = 2 - a + (a / 4)
        if (y < 1583) b = 0
        if (y == 1582) {
            if (m > 10) b = -10
            if (m == 10) {
                b = 0
                if (d > 4) b = -10
            }
        }

        val jd = (365.25 * (y + 4716)).toLong() +
                (30.6001 * (m + 1)).toLong() +
                d + b - 1524 + dayAdjustment

        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val remL = l - 10631 * n + 354
        val j = (((10985 - remL) / 5316).toInt()) * ((50 * remL / 17719).toInt()) +
                ((remL / 5670).toInt()) * ((43 * remL / 15238).toInt())
        val remL2 = remL - (((30 - j) / 15).toInt()) * ((17719 * j / 50).toInt()) -
                ((j / 16).toInt()) * ((15238 * j / 43).toInt()) + 29
        val hMonth = (24 * remL2 / 709).toInt()
        val hDay = (remL2 - (709 * hMonth / 24)).toInt()
        val hYear = (30 * n + j - 30)

        val boundedMonth = hMonth.coerceIn(1, 12)
        val boundedDay = hDay.coerceIn(1, 30)

        return Triple(hYear, boundedMonth, boundedDay)
    }

    fun getFormattedHijriDate(calendar: Calendar, dayAdjustment: Int = 0): String {
        val (hYear, hMonth, hDay) = toHijri(calendar, dayAdjustment)
        val monthName = hijriMonthNames[hMonth - 1]
        val dayName = getDayOfWeekArabic(calendar)
        return "$dayName، $hDay $monthName $hYear هـ"
    }

    fun getFormattedGregorianDate(calendar: Calendar): String {
        val dayName = getDayOfWeekArabic(calendar)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$dayName، $day/$month/$year م"
    }
}
