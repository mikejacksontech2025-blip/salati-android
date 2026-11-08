package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlarmEntity
import com.example.data.AppDatabase
import com.example.data.SoundEntity
import com.example.data.UserPreferencesRepository
import com.example.prayer.CalculationMethod
import com.example.prayer.CityLocation
import com.example.prayer.DayPrayerSchedule
import com.example.prayer.JuristicMethod
import com.example.prayer.PrayerCalculator
import com.example.prayer.PrayerType
import com.example.service.AlarmScheduler
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.TimeZone

class SalatiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val prefs = UserPreferencesRepository(application)

    // Current Prayer Schedule
    private val _daySchedule = MutableStateFlow<DayPrayerSchedule?>(null)
    val daySchedule: StateFlow<DayPrayerSchedule?> = _daySchedule.asStateFlow()

    // Preferences exposed
    val currentCity: StateFlow<CityLocation> = prefs.currentCity
    val calculationMethod: StateFlow<CalculationMethod> = prefs.calculationMethod
    val juristicMethod: StateFlow<JuristicMethod> = prefs.juristicMethod
    val adhanVolume: StateFlow<Float> = prefs.adhanVolume
    val masterAdhanEnabled: StateFlow<Boolean> = prefs.masterAdhanEnabled
    val darkMode: StateFlow<String> = prefs.darkMode

    // Alarms list
    val alarms: StateFlow<List<AlarmEntity>> = db.alarmDao().getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sounds list
    val sounds: StateFlow<List<SoundEntity>> = db.soundDao().getAllSounds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio Preview State
    private var previewMediaPlayer: MediaPlayer? = null
    private val _playingSoundId = MutableStateFlow<Long?>(null)
    val playingSoundId: StateFlow<Long?> = _playingSoundId.asStateFlow()

    // Qibla Angle
    private val _qiblaBearing = MutableStateFlow(0.0)
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    // Tasbeeh Counter
    private val _tasbeehCount = MutableStateFlow(0)
    val tasbeehCount: StateFlow<Int> = _tasbeehCount.asStateFlow()

    private val _tasbeehTarget = MutableStateFlow(33)
    val tasbeehTarget: StateFlow<Int> = _tasbeehTarget.asStateFlow()

    init {
        // Start 1-second ticker loop to update countdown & prayer times
        viewModelScope.launch {
            while (true) {
                updateSchedule()
                delay(1000)
            }
        }
    }

    fun updateSchedule() {
        val city = prefs.currentCity.value
        val enabledMap = PrayerType.allSchedule.associateWith { prefs.isAdhanEnabled(it) }

        val schedule = PrayerCalculator.buildDaySchedule(
            currentMillis = System.currentTimeMillis(),
            latitude = city.latitude,
            longitude = city.longitude,
            timeZoneId = city.timeZoneId,
            method = prefs.calculationMethod.value,
            juristic = prefs.juristicMethod.value,
            enabledAdhans = enabledMap
        )
        _daySchedule.value = schedule
        _qiblaBearing.value = PrayerCalculator.calculateQibla(city.latitude, city.longitude)
    }

    // --- Sound Preview ---
    fun togglePlaySoundPreview(sound: SoundEntity) {
        if (_playingSoundId.value == sound.id) {
            stopSoundPreview()
        } else {
            playSoundPreview(sound)
        }
    }

    private fun playSoundPreview(sound: SoundEntity) {
        stopSoundPreview()
        try {
            previewMediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                val vol = prefs.adhanVolume.value
                setVolume(vol, vol)

                var loaded = false
                if (sound.filePath.startsWith("raw/")) {
                    val rawName = sound.filePath.removePrefix("raw/")
                    val resId = getApplication<Application>().resources.getIdentifier(
                        rawName, "raw", getApplication<Application>().packageName
                    )
                    if (resId != 0) {
                        val afd = getApplication<Application>().resources.openRawResourceFd(resId)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        loaded = true
                    }
                } else {
                    val file = File(sound.filePath)
                    if (file.exists()) {
                        setDataSource(file.absolutePath)
                        loaded = true
                    }
                }

                if (loaded) {
                    if (sound.trimStartMs > 0) {
                        seekTo(sound.trimStartMs)
                    }
                    setOnCompletionListener {
                        stopSoundPreview()
                    }
                    prepare()
                    start()
                    _playingSoundId.value = sound.id
                }
            }
        } catch (e: Exception) {
            Log.e("SalatiViewModel", "Failed to play sound preview", e)
            stopSoundPreview()
        }
    }

    fun stopSoundPreview() {
        try {
            previewMediaPlayer?.stop()
            previewMediaPlayer?.release()
        } catch (_: Exception) {}
        previewMediaPlayer = null
        _playingSoundId.value = null
    }

    // --- Sound Import & Management ---
    fun importSoundFromUri(uri: Uri, displayName: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val soundsDir = File(context.filesDir, "custom_sounds").apply { mkdirs() }
                val targetFile = File(soundsDir, "sound_${System.currentTimeMillis()}.mp3")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                var durationSec = 10
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(targetFile.absolutePath)
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durMs = durStr?.toLongOrNull() ?: 10000L
                    durationSec = (durMs / 1000).toInt()
                    retriever.release()
                } catch (_: Exception) {}

                val newSound = SoundEntity(
                    name = displayName.ifBlank { "صوت مستورد" },
                    category = category,
                    filePath = targetFile.absolutePath,
                    durationSec = durationSec,
                    isBuiltIn = false
                )
                db.soundDao().insertSound(newSound)
            } catch (e: Exception) {
                Log.e("SalatiViewModel", "Error importing sound", e)
            }
        }
    }

    fun updateSound(sound: SoundEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.soundDao().updateSound(sound)
        }
    }

    fun deleteSound(sound: SoundEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_playingSoundId.value == sound.id) {
                withContext(Dispatchers.Main) { stopSoundPreview() }
            }
            if (!sound.isBuiltIn && sound.filePath.isNotEmpty()) {
                try {
                    val file = File(sound.filePath)
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
            db.soundDao().deleteSound(sound)
        }
    }

    // --- Alarms Management ---
    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = db.alarmDao().insertAlarm(alarm)
            val updated = if (alarm.id == 0L) alarm.copy(id = id) else alarm
            withContext(Dispatchers.Main) {
                AlarmScheduler.scheduleRegularAlarm(getApplication(), updated)
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            db.alarmDao().updateAlarm(updated)
            withContext(Dispatchers.Main) {
                AlarmScheduler.scheduleRegularAlarm(getApplication(), updated)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.alarmDao().deleteAlarm(alarm)
            withContext(Dispatchers.Main) {
                AlarmScheduler.cancelRegularAlarm(getApplication(), alarm.id)
            }
        }
    }

    // --- Prayer Adhan Settings ---
    fun togglePrayerAdhan(prayer: PrayerType) {
        val current = prefs.isAdhanEnabled(prayer)
        prefs.setAdhanEnabled(prayer, !current)
        updateSchedule()
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun toggleMasterAdhan() {
        val current = prefs.masterAdhanEnabled.value
        prefs.setMasterAdhanEnabled(!current)
        updateSchedule()
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun setPrayerSound(prayer: PrayerType, soundUri: String, isFull: Boolean) {
        prefs.setAdhanSoundUri(prayer, soundUri)
        prefs.setFullAdhan(prayer, isFull)
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun setAllPrayersSound(soundUri: String, isFull: Boolean) {
        PrayerType.mainPrayers.forEach {
            prefs.setAdhanSoundUri(it, soundUri)
            prefs.setFullAdhan(it, isFull)
        }
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun setVolume(volume: Float) {
        prefs.setAdhanVolume(volume)
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.setCalculationMethod(method)
        updateSchedule()
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun setJuristicMethod(juristic: JuristicMethod) {
        prefs.setJuristicMethod(juristic)
        updateSchedule()
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun selectCity(city: CityLocation) {
        prefs.saveCity(city)
        updateSchedule()
        AlarmScheduler.scheduleNextPrayerAlarm(getApplication())
    }

    fun setDarkMode(mode: String) {
        prefs.setDarkMode(mode)
    }

    // --- GPS Location Detection ---
    fun detectLocationFromGps(context: Context, onResult: (Boolean, String) -> Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            onResult(false, "يرجى منح إذن الموقع لتحديد مدينتك تلقائياً")
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    var cityName = "الموقع الحالي"
                    try {
                        val geocoder = Geocoder(context, Locale("ar"))
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "الموقع الحالي"
                        }
                    } catch (_: Exception) {}

                    val detectedCity = CityLocation(
                        nameArabic = cityName,
                        countryArabic = "محدد تلقائياً",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timeZoneId = TimeZone.getDefault().id
                    )
                    selectCity(detectedCity)
                    onResult(true, "تم تحديد موقعك بنجاح: $cityName")
                } else {
                    onResult(false, "تعذر جلب إحداثيات الموقع حالياً، يرجى تفعيل الـ GPS")
                }
            }.addOnFailureListener {
                onResult(false, "فشل جلب الموقع: ${it.localizedMessage}")
            }
        } catch (e: Exception) {
            onResult(false, "خطأ: ${e.localizedMessage}")
        }
    }

    // --- Tasbeeh counter ---
    fun incrementTasbeeh() {
        if (_tasbeehCount.value + 1 >= _tasbeehTarget.value) {
            _tasbeehCount.value = 0
        } else {
            _tasbeehCount.value += 1
        }
    }

    fun resetTasbeeh() {
        _tasbeehCount.value = 0
    }

    fun setTasbeehTarget(target: Int) {
        _tasbeehTarget.value = target
        _tasbeehCount.value = 0
    }

    override fun onCleared() {
        stopSoundPreview()
        super.onCleared()
    }
}
