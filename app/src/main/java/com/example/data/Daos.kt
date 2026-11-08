package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)
}

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds ORDER BY category ASC, name ASC")
    fun getAllSounds(): Flow<List<SoundEntity>>

    @Query("SELECT * FROM sounds WHERE category = :category ORDER BY name ASC")
    fun getSoundsByCategory(category: String): Flow<List<SoundEntity>>

    @Query("SELECT * FROM sounds WHERE id = :id")
    suspend fun getSoundById(id: Long): SoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: SoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sounds: List<SoundEntity>)

    @Update
    suspend fun updateSound(sound: SoundEntity)

    @Delete
    suspend fun deleteSound(sound: SoundEntity)

    @Query("SELECT COUNT(*) FROM sounds")
    suspend fun getSoundsCount(): Int
}
