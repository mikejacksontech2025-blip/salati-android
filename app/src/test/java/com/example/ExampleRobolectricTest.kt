package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.prayer.CityLocations
import com.example.prayer.PrayerCalculator
import com.example.prayer.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("صَلَاتِي", appName)
    }

    @Test
    fun `test offline prayer calculation for Makkah`() {
        val makkah = CityLocations.defaultCity
        val cal = Calendar.getInstance()
        val times = PrayerCalculator.calculatePrayerTimes(
            calendar = cal,
            latitude = makkah.latitude,
            longitude = makkah.longitude,
            timeZoneId = makkah.timeZoneId
        )

        assertNotNull(times[PrayerType.FAJR])
        assertNotNull(times[PrayerType.DHUHR])
        assertNotNull(times[PrayerType.ASR])
        assertNotNull(times[PrayerType.MAGHRIB])
        assertNotNull(times[PrayerType.ISHA])

        // Verify logical chronological order
        val fajr = times[PrayerType.FAJR]!!
        val sunrise = times[PrayerType.SUNRISE]!!
        val dhuhr = times[PrayerType.DHUHR]!!
        val asr = times[PrayerType.ASR]!!
        val maghrib = times[PrayerType.MAGHRIB]!!
        val isha = times[PrayerType.ISHA]!!

        assertTrue("Sunrise must be after Fajr", sunrise > fajr)
        assertTrue("Dhuhr must be after Sunrise", dhuhr > sunrise)
        assertTrue("Asr must be after Dhuhr", asr > dhuhr)
        assertTrue("Maghrib must be after Asr", maghrib > asr)
        assertTrue("Isha must be after Maghrib", isha > maghrib)
    }
}
