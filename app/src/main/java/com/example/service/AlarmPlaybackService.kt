package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AlarmPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (action == ACTION_STOP) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_SNOOZE) {
            val snoozeMin = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 10)
            snoozeAlarm(snoozeMin)
            stopSelf()
            return START_NOT_STICKY
        }

        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "تنبيه صَلَاتِي"
        val soundUriStr = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI) ?: "raw/adhan_takbeer"
        val isPrayer = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_PRAYER, true)
        val shouldVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)

        _activeAlarmState.value = ActiveAlarmInfo(
            title = title,
            isPrayer = isPrayer,
            isPlaying = true
        )

        val notification = buildForegroundNotification(title, isPrayer)
        startForeground(NOTIFICATION_ID, notification)

        playAudio(soundUriStr, isPrayer)

        if (shouldVibrate) {
            startVibration()
        }

        return START_STICKY
    }

    private fun playAudio(soundUriStr: String, isPrayer: Boolean) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                val prefs = UserPreferencesRepository(applicationContext)
                val volume = prefs.adhanVolume.value
                setVolume(volume, volume)

                // Try raw resource or file
                var loaded = false
                if (soundUriStr.startsWith("raw/")) {
                    val rawName = soundUriStr.removePrefix("raw/")
                    val resId = resources.getIdentifier(rawName, "raw", packageName)
                    if (resId != 0) {
                        val afd = resources.openRawResourceFd(resId)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        loaded = true
                    }
                } else if (soundUriStr.isNotEmpty()) {
                    val file = File(soundUriStr)
                    if (file.exists()) {
                        setDataSource(file.absolutePath)
                        loaded = true
                    } else {
                        try {
                            setDataSource(applicationContext, Uri.parse(soundUriStr))
                            loaded = true
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed loading custom sound uri, fallback to default", e)
                        }
                    }
                }

                if (!loaded) {
                    val afd = resources.openRawResourceFd(R.raw.adhan_takbeer)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                }

                isLooping = !isPrayer // Regular alarm loops until dismissed, Adhan plays once

                setOnCompletionListener {
                    if (isPrayer) {
                        stopAlarm()
                        stopSelf()
                    }
                }

                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm audio", e)
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 800, 500, 800, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling vibration", e)
        }

        _activeAlarmState.value = null
    }

    private fun snoozeAlarm(minutes: Int) {
        stopAlarm()
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmScheduler.ACTION_REGULAR_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, "غفوة التنبيه ($minutes د)")
            putExtra(AlarmScheduler.EXTRA_SOUND_URI, "raw/alarm_gentle")
            putExtra(AlarmScheduler.EXTRA_VIBRATE, true)
            putExtra(AlarmScheduler.EXTRA_IS_PRAYER, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
        alarmManager?.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun buildForegroundNotification(title: String, isPrayer: Boolean): Notification {
        val fullScreenIntent = Intent(this, AlarmFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_IS_PRAYER, isPrayer)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmPlaybackService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, 10)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isPrayer) CHANNEL_PRAYER else CHANNEL_ALARM

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_salati_icon)
            .setContentTitle("صَلَاتِي – $title")
            .setContentText(if (isPrayer) "حي على الصلاة، حي على الفلاح" else "حان موعد المنبه")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "إيقاف", stopPendingIntent)
            .addAction(0, "غفوة (10 د)", snoozePendingIntent)
            .build()
    }

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER,
                "أذان ومواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات وأذان الصلوات الخمس"
                enableVibration(true)
                setSound(null, null) // Sound is played by MediaPlayer
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "المنبهات اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "منبهات الاستيقاظ والمهام اليومية"
                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AlarmPlaybackService"
        private const val NOTIFICATION_ID = 7771
        const val CHANNEL_PRAYER = "salati_prayer_channel"
        const val CHANNEL_ALARM = "salati_alarm_channel"

        const val ACTION_STOP = "com.example.salati.ACTION_STOP"
        const val ACTION_SNOOZE = "com.example.salati.ACTION_SNOOZE"

        data class ActiveAlarmInfo(
            val title: String,
            val isPrayer: Boolean,
            val isPlaying: Boolean
        )

        private val _activeAlarmState = MutableStateFlow<ActiveAlarmInfo?>(null)
        val activeAlarmState: StateFlow<ActiveAlarmInfo?> = _activeAlarmState.asStateFlow()

        fun stop(context: Context) {
            val intent = Intent(context, AlarmPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun snooze(context: Context, minutes: Int = 10) {
            val intent = Intent(context, AlarmPlaybackService::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(AlarmScheduler.EXTRA_SNOOZE_MINUTES, minutes)
            }
            context.startService(intent)
        }
    }
}
