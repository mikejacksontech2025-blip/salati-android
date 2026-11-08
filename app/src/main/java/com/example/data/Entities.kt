package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String = "0,1,2,3,4,5,6", // 0: Sun, 1: Mon, ... 6: Sat
    val isEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val vibrate: Boolean = true,
    val soundUri: String = "", // URI or file path or "default"
    val soundName: String = "المنبه الافتراضي",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "أذان", "منبه", "تنبيه"
    val filePath: String, // Local internal file path or raw resource name
    val durationSec: Int = 0,
    val isBuiltIn: Boolean = false,
    val trimStartMs: Int = 0,
    val trimEndMs: Int = 0,
    val volumePercent: Int = 100
)
