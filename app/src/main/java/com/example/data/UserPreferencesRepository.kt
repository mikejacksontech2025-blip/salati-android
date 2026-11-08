package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.prayer.CalculationMethod
import com.example.prayer.CityLocation
import com.example.prayer.CityLocations
import com.example.prayer.JuristicMethod
import com.example.prayer.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("salati_user_prefs", Context.MODE_PRIVATE)

    private val _currentCity = MutableStateFlow(loadCity())
    val currentCity: StateFlow<CityLocation> = _currentCity.asStateFlow()

    private val _autoLocation = MutableStateFlow(prefs.getBoolean(KEY_AUTO_LOCATION, false))
    val autoLocation: StateFlow<Boolean> = _autoLocation.asStateFlow()

    private val _calculationMethod = MutableStateFlow(loadCalculationMethod())
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _juristicMethod = MutableStateFlow(loadJuristicMethod())
    val juristicMethod: StateFlow<JuristicMethod> = _juristicMethod.asStateFlow()

    private val _adhanVolume = MutableStateFlow(prefs.getFloat(KEY_ADHAN_VOLUME, 0.9f))
    val adhanVolume: StateFlow<Float> = _adhanVolume.asStateFlow()

    private val _masterAdhanEnabled = MutableStateFlow(prefs.getBoolean(KEY_MASTER_ADHAN, true))
    val masterAdhanEnabled: StateFlow<Boolean> = _masterAdhanEnabled.asStateFlow()

    private val _darkMode = MutableStateFlow(prefs.getString(KEY_DARK_MODE, "SYSTEM") ?: "SYSTEM")
    val darkMode: StateFlow<String> = _darkMode.asStateFlow()

    fun isAdhanEnabled(prayer: PrayerType): Boolean {
        if (!_masterAdhanEnabled.value) return false
        return prefs.getBoolean("adhan_enabled_${prayer.id}", prayer != PrayerType.SUNRISE)
    }

    fun setAdhanEnabled(prayer: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("adhan_enabled_${prayer.id}", enabled).apply()
    }

    fun getAdhanSoundUri(prayer: PrayerType): String {
        return prefs.getString("adhan_sound_${prayer.id}", "raw/adhan_takbeer") ?: "raw/adhan_takbeer"
    }

    fun setAdhanSoundUri(prayer: PrayerType, uri: String) {
        prefs.edit().putString("adhan_sound_${prayer.id}", uri).apply()
    }

    fun isFullAdhan(prayer: PrayerType): Boolean {
        return prefs.getBoolean("adhan_full_${prayer.id}", true)
    }

    fun setFullAdhan(prayer: PrayerType, full: Boolean) {
        prefs.edit().putBoolean("adhan_full_${prayer.id}", full).apply()
    }

    fun saveCity(city: CityLocation) {
        prefs.edit()
            .putString(KEY_CITY_NAME, city.nameArabic)
            .putString(KEY_CITY_COUNTRY, city.countryArabic)
            .putString(KEY_CITY_LAT, city.latitude.toString())
            .putString(KEY_CITY_LNG, city.longitude.toString())
            .putString(KEY_CITY_TZ, city.timeZoneId)
            .apply()
        _currentCity.value = city
    }

    private fun loadCity(): CityLocation {
        val name = prefs.getString(KEY_CITY_NAME, null) ?: return CityLocations.defaultCity
        val country = prefs.getString(KEY_CITY_COUNTRY, "السعودية") ?: "السعودية"
        val lat = prefs.getString(KEY_CITY_LAT, "21.4225")?.toDoubleOrNull() ?: 21.4225
        val lng = prefs.getString(KEY_CITY_LNG, "39.8262")?.toDoubleOrNull() ?: 39.8262
        val tz = prefs.getString(KEY_CITY_TZ, "Asia/Riyadh") ?: "Asia/Riyadh"
        return CityLocation(name, country, lat, lng, tz)
    }

    fun setAutoLocation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOCATION, enabled).apply()
        _autoLocation.value = enabled
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
        _calculationMethod.value = method
    }

    private fun loadCalculationMethod(): CalculationMethod {
        val name = prefs.getString(KEY_CALC_METHOD, CalculationMethod.UMM_AL_QURA.name)
        return try {
            CalculationMethod.valueOf(name ?: CalculationMethod.UMM_AL_QURA.name)
        } catch (_: Exception) {
            CalculationMethod.UMM_AL_QURA
        }
    }

    fun setJuristicMethod(juristic: JuristicMethod) {
        prefs.edit().putString(KEY_JURISTIC_METHOD, juristic.name).apply()
        _juristicMethod.value = juristic
    }

    private fun loadJuristicMethod(): JuristicMethod {
        val name = prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.SHAFI.name)
        return try {
            JuristicMethod.valueOf(name ?: JuristicMethod.SHAFI.name)
        } catch (_: Exception) {
            JuristicMethod.SHAFI
        }
    }

    fun setAdhanVolume(volume: Float) {
        val clamped = volume.coerceIn(0.1f, 1.0f)
        prefs.edit().putFloat(KEY_ADHAN_VOLUME, clamped).apply()
        _adhanVolume.value = clamped
    }

    fun setMasterAdhanEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_ADHAN, enabled).apply()
        _masterAdhanEnabled.value = enabled
    }

    fun setDarkMode(mode: String) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply()
        _darkMode.value = mode
    }

    companion object {
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_CITY_COUNTRY = "city_country"
        private const val KEY_CITY_LAT = "city_lat"
        private const val KEY_CITY_LNG = "city_lng"
        private const val KEY_CITY_TZ = "city_tz"
        private const val KEY_AUTO_LOCATION = "auto_location"
        private const val KEY_CALC_METHOD = "calc_method"
        private const val KEY_JURISTIC_METHOD = "juristic_method"
        private const val KEY_ADHAN_VOLUME = "adhan_volume"
        private const val KEY_MASTER_ADHAN = "master_adhan"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
