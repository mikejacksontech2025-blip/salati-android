package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast action: $action")

        // Reschedule next prayer
        AlarmScheduler.scheduleNextPrayerAlarm(context)

        // Reschedule all active regular alarms from Room DB
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val enabledAlarms = db.alarmDao().getEnabledAlarms()
                for (alarm in enabledAlarms) {
                    AlarmScheduler.scheduleRegularAlarm(context, alarm)
                }
                Log.d("BootReceiver", "Rescheduled ${enabledAlarms.size} regular alarms successfully")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms on boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
