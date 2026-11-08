package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.SoundEntity
import com.example.service.AlarmPlaybackService
import com.example.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SalatiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        seedDefaultSoundsIfNeeded()
        // Schedule next prayer on application launch
        AlarmScheduler.scheduleNextPrayerAlarm(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                AlarmPlaybackService.CHANNEL_PRAYER,
                "مواقيت وأذان الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الأذان والصلوات الخمس بدقة متناهية"
                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val alarmChannel = NotificationChannel(
                AlarmPlaybackService.CHANNEL_ALARM,
                "المنبهات اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "منبهات الاستيقاظ والمهام اليومية"
                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(prayerChannel)
            manager.createNotificationChannel(alarmChannel)
        }
    }

    private fun seedDefaultSoundsIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(this@SalatiApp)
                if (db.soundDao().getSoundsCount() == 0) {
                    val defaultSounds = listOf(
                        SoundEntity(
                            name = "أذان الحرم المكي الشريف",
                            category = "أذان",
                            filePath = "raw/adhan_takbeer",
                            durationSec = 20,
                            isBuiltIn = true
                        ),
                        SoundEntity(
                            name = "نداء الفجر (الصلاة خير من النوم)",
                            category = "أذان",
                            filePath = "raw/fajr_prayer_tone",
                            durationSec = 15,
                            isBuiltIn = true
                        ),
                        SoundEntity(
                            name = "تكبيرات الصلاة والتنبيه القصير",
                            category = "تنبيه",
                            filePath = "raw/adhan_short",
                            durationSec = 7,
                            isBuiltIn = true
                        ),
                        SoundEntity(
                            name = "رنين المنبه الهادئ",
                            category = "منبه",
                            filePath = "raw/alarm_gentle",
                            durationSec = 9,
                            isBuiltIn = true
                        )
                    )
                    db.soundDao().insertAll(defaultSounds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
