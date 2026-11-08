package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "AlarmReceiver received action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Salati:AlarmWakeLock"
        )
        wakeLock?.acquire(10 * 1000L) // 10 seconds timeout

        try {
            val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "صَلَاتِي"
            val soundUri = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI) ?: "raw/adhan_takbeer"
            val isPrayer = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_PRAYER, true)
            val vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)
            val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 10)

            // Start Foreground Playback Service
            val serviceIntent = Intent(context, AlarmPlaybackService::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
                putExtra(AlarmScheduler.EXTRA_SOUND_URI, soundUri)
                putExtra(AlarmScheduler.EXTRA_IS_PRAYER, isPrayer)
                putExtra(AlarmScheduler.EXTRA_VIBRATE, vibrate)
                putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            }
            ContextCompat.startForegroundService(context, serviceIntent)

            // Open Full-screen alarm activity
            val fullScreenIntent = Intent(context, AlarmFullScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
                putExtra(AlarmScheduler.EXTRA_IS_PRAYER, isPrayer)
                putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
            }
            context.startActivity(fullScreenIntent)

            // If prayer alarm triggered, schedule the next prayer automatically
            if (isPrayer) {
                AlarmScheduler.scheduleNextPrayerAlarm(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling alarm receive", e)
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
