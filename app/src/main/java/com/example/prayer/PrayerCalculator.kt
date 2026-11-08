package com.example.prayer

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    /**
     * Calculates Qibla direction bearing in degrees from North (0 - 360).
     */
    fun calculateQibla(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - longitude)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    /**
     * Compute prayer times for a given day, location, and calculation parameters.
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        juristic: JuristicMethod = JuristicMethod.SHAFI
    ): Map<PrayerType, Long> {
        val tz = TimeZone.getTimeZone(timeZoneId)
        val cal = Calendar.getInstance(tz).apply {
            timeInMillis = calendar.timeInMillis
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Date
        val jd = computeJulianDate(year, month, day)
        val d = jd - 2451545.0 // Days since J2000.0

        // TimeZone offset in hours
        val timeZoneOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0

        // Sun's astronomical position
        val (eqTime, declination) = computeSunPosition(d)

        // Solar noon (transit) in hours
        val transit = 12.0 + timeZoneOffsetHours - (longitude / 15.0) - (eqTime / 60.0)

        // Sunrise & Sunset angle (-0.8333 degrees)
        val sunAlt = -0.8333
        val sunriseHourAngle = computeHourAngle(sunAlt, latitude, declination)
        val sunrise = transit - (sunriseHourAngle / 15.0)
        val sunset = transit + (sunriseHourAngle / 15.0)

        // Fajr
        val fajrHourAngle = computeHourAngle(-method.fajrAngle, latitude, declination)
        val fajr = transit - (fajrHourAngle / 15.0)

        // Dhuhr (solar noon + slight buffer e.g. 1-2 minutes past zenith)
        val dhuhr = transit + (1.0 / 60.0)

        // Asr
        val asrAlt = Math.toDegrees(atan(1.0 / (juristic.shadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = computeHourAngle(asrAlt, latitude, declination)
        val asr = transit + (asrHourAngle / 15.0)

        // Maghrib
        val maghrib = sunset + (1.0 / 60.0)

        // Isha
        val isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaHourAngle = computeHourAngle(-method.ishaAngle, latitude, declination)
            transit + (ishaHourAngle / 15.0)
        }

        val result = mutableMapOf<PrayerType, Long>()
        result[PrayerType.FAJR] = hourToMillis(cal, fajr)
        result[PrayerType.SUNRISE] = hourToMillis(cal, sunrise)
        result[PrayerType.DHUHR] = hourToMillis(cal, dhuhr)
        result[PrayerType.ASR] = hourToMillis(cal, asr)
        result[PrayerType.MAGHRIB] = hourToMillis(cal, maghrib)
        result[PrayerType.ISHA] = hourToMillis(cal, isha)

        return result
    }

    private fun computeJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun computeSunPosition(d: Double): Pair<Double, Double> {
        // Mean anomaly
        val m = fixAngle(357.529 + 0.98560028 * d)
        // Mean longitude
        val l0 = fixAngle(280.459 + 0.98564736 * d)
        // Ecliptic longitude
        val c = 1.915 * sin(Math.toRadians(m)) + 0.020 * sin(Math.toRadians(2 * m))
        val lambda = fixAngle(l0 + c)

        // Obliquity of ecliptic
        val eps = 23.439 - 0.00000036 * d

        // Declination
        val sinDelta = sin(Math.toRadians(eps)) * sin(Math.toRadians(lambda))
        val delta = Math.toDegrees(asin(sinDelta))

        // Equation of time in minutes
        val y = tan(Math.toRadians(eps) / 2.0).let { it * it }
        val sin2L0 = sin(2 * Math.toRadians(l0))
        val sinM = sin(Math.toRadians(m))
        val cos2L0 = cos(2 * Math.toRadians(l0))
        val sin4L0 = sin(4 * Math.toRadians(l0))
        val sin2M = sin(2 * Math.toRadians(m))

        val eqTimeRad = y * sin2L0 - 2 * 0.0167 * sinM + 4 * 0.0167 * y * sinM * cos2L0 -
                0.5 * y * y * sin4L0 - 1.25 * 0.0167 * 0.0167 * sin2M
        val eqTimeMin = Math.toDegrees(eqTimeRad) * 4.0

        return Pair(eqTimeMin, delta)
    }

    private fun computeHourAngle(alt: Double, lat: Double, delta: Double): Double {
        val latRad = Math.toRadians(lat)
        val deltaRad = Math.toRadians(delta)
        val altRad = Math.toRadians(alt)

        val cosHourAngle = (sin(altRad) - sin(latRad) * sin(deltaRad)) / (cos(latRad) * cos(deltaRad))
        val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun hourToMillis(baseCal: Calendar, hourFraction: Double): Long {
        val totalSec = (hourFraction * 3600.0).toLong()
        val hours = (totalSec / 3600).toInt()
        val minutes = ((totalSec % 3600) / 60).toInt()
        val seconds = (totalSec % 60).toInt()

        val cal = baseCal.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, hours.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, minutes.coerceIn(0, 59))
        cal.set(Calendar.SECOND, seconds.coerceIn(0, 59))
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatTime12hArabic(timeMillis: Long, timeZoneId: String): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply {
            this.timeInMillis = timeMillis
        }
        var h = cal.get(Calendar.HOUR)
        if (h == 0) h = 12
        val m = cal.get(Calendar.MINUTE)
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "ص" else "م"
        return String.format(Locale.getDefault(), "%02d:%02d %s", h, m, amPm)
    }

    /**
     * Builds full day schedule, identifying passed, active and next prayer.
     */
    fun buildDaySchedule(
        currentMillis: Long,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        juristic: JuristicMethod = JuristicMethod.SHAFI,
        enabledAdhans: Map<PrayerType, Boolean> = emptyMap()
    ): DayPrayerSchedule {
        val nowCal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply {
            timeInMillis = currentMillis
        }

        val todayTimes = calculatePrayerTimes(nowCal, latitude, longitude, timeZoneId, method, juristic)

        // Next prayer detection
        var nextPrayerType: PrayerType? = null
        var nextPrayerTime: Long = 0L

        for (type in PrayerType.allSchedule) {
            val pTime = todayTimes[type] ?: 0L
            if (pTime > currentMillis) {
                nextPrayerType = type
                nextPrayerTime = pTime
                break
            }
        }

        // If all prayers today passed, next is tomorrow's Fajr
        if (nextPrayerType == null) {
            val tomorrowCal = (nowCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowTimes = calculatePrayerTimes(tomorrowCal, latitude, longitude, timeZoneId, method, juristic)
            nextPrayerType = PrayerType.FAJR
            nextPrayerTime = tomorrowTimes[PrayerType.FAJR] ?: (currentMillis + 8 * 3600000L)
        }

        val items = PrayerType.allSchedule.map { type ->
            val pTime = todayTimes[type] ?: 0L
            val isPassed = currentMillis >= pTime
            val isNext = type == nextPrayerType
            val isAdhanOn = enabledAdhans[type] ?: (type != PrayerType.SUNRISE)
            PrayerTimeItem(
                type = type,
                timeMillis = pTime,
                formattedTime12h = formatTime12hArabic(pTime, timeZoneId),
                isPassed = isPassed,
                isNext = isNext,
                isAdhanEnabled = isAdhanOn
            )
        }

        val nextItem = items.find { it.type == nextPrayerType } ?: PrayerTimeItem(
            type = nextPrayerType,
            timeMillis = nextPrayerTime,
            formattedTime12h = formatTime12hArabic(nextPrayerTime, timeZoneId),
            isPassed = false,
            isNext = true,
            isAdhanEnabled = enabledAdhans[nextPrayerType] ?: true
        )

        val remainingMillis = (nextPrayerTime - currentMillis).coerceAtLeast(0L)

        return DayPrayerSchedule(
            dateFormattedGregorian = HijriCalendarHelper.getFormattedGregorianDate(nowCal),
            dateFormattedHijri = HijriCalendarHelper.getFormattedHijriDate(nowCal),
            prayers = items,
            nextPrayer = nextItem,
            remainingMillisToNext = remainingMillis
        )
    }
}
