package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AlarmEntity
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.prayer.PrayerCalculator
import com.example.prayer.PrayerType
import java.util.Calendar
import java.util.TimeZone

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    const val ACTION_PRAYER_ALARM = "com.example.salati.ACTION_PRAYER_ALARM"
    const val ACTION_REGULAR_ALARM = "com.example.salati.ACTION_REGULAR_ALARM"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_TITLE = "extra_alarm_title"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"
    const val EXTRA_SOUND_URI = "extra_sound_uri"
    const val EXTRA_VIBRATE = "extra_vibrate"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    const val EXTRA_IS_PRAYER = "extra_is_prayer"

    fun scheduleNextPrayerAlarm(context: Context) {
        val prefs = UserPreferencesRepository(context)
        if (!prefs.masterAdhanEnabled.value) {
            Log.d(TAG, "Master adhan is disabled, skipping prayer schedule")
            return
        }

        val city = prefs.currentCity.value
        val method = prefs.calculationMethod.value
        val juristic = prefs.juristicMethod.value

        val now = System.currentTimeMillis()
        val schedule = PrayerCalculator.buildDaySchedule(
            currentMillis = now,
            latitude = city.latitude,
            longitude = city.longitude,
            timeZoneId = city.timeZoneId,
            method = method,
            juristic = juristic
        )

        val next = schedule.nextPrayer ?: return
        if (!prefs.isAdhanEnabled(next.type)) {
            Log.d(TAG, "Adhan disabled for ${next.type.arabicName}")
            return
        }

        val soundUri = prefs.getAdhanSoundUri(next.type)
        val isFull = prefs.isFullAdhan(next.type)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_IS_PRAYER, true)
            putExtra(EXTRA_PRAYER_ID, next.type.id)
            putExtra(EXTRA_ALARM_TITLE, "حان الآن وقت صلاة ${next.type.arabicName}")
            putExtra(EXTRA_SOUND_URI, if (isFull) soundUri else "raw/adhan_short")
            putExtra(EXTRA_VIBRATE, true)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }

        val requestCode = 1000 + next.type.ordinal
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            val showIntent = Intent(context, AlarmFullScreenActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                requestCode + 500,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmClockInfo = AlarmManager.AlarmClockInfo(next.timeMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm clock for prayer ${next.type.arabicName} at ${next.timeMillis}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing, falling back to setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeMillis, pendingIntent)
        }
    }

    fun scheduleRegularAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelRegularAlarm(context, alarm.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerTime = computeNextTriggerTime(alarm.hour, alarm.minute, alarm.daysOfWeek)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REGULAR_ALARM
            putExtra(EXTRA_IS_PRAYER, false)
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_TITLE, alarm.title)
            putExtra(EXTRA_SOUND_URI, alarm.soundUri)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
            putExtra(EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
        }

        val requestCode = (2000 + alarm.id).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, AlarmFullScreenActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 5000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm '${alarm.title}' for $triggerTime")
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelRegularAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REGULAR_ALARM
        }
        val requestCode = (2000 + alarmId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun computeNextTriggerTime(hour: Int, minute: Int, daysOfWeek: String): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val activeDays = if (daysOfWeek.isBlank()) {
            listOf(0, 1, 2, 3, 4, 5, 6)
        } else {
            daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        }

        val now = System.currentTimeMillis()
        // If today at (hour:minute) is already past, advance by 1 day
        if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Loop up to 7 days to match selected repeat day (0: Sunday .. 6: Saturday)
        for (i in 0..7) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            if (activeDays.contains(dayOfWeek)) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }
}
