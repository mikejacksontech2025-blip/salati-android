package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlarmEntity
import com.example.data.SoundEntity
import com.example.ui.SalatiViewModel
import com.example.ui.components.LuxuryCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDarkCard
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun AlarmsScreen(
    viewModel: SalatiViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val sounds by viewModel.sounds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("alarms_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionHeader(
                    title = "المنبهات اليومية والمستقلة",
                    subtitle = "تنبيهات دقيقة تعمل حتى عندما يكون الهاتف مقفلاً",
                    icon = Icons.Default.Alarm
                )
            }

            if (alarms.isEmpty()) {
                item {
                    LuxuryCard(modifier = Modifier.padding(vertical = 24.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22D4AF37)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "لا توجد منبهات مضافة بعد",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اضغط على الزر أدناه لإضافة منبه للاستيقاظ أو قيام الليل أو العمل",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmItemCard(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onEdit = {
                            editingAlarm = alarm
                            showAddDialog = true
                        },
                        onDelete = { viewModel.deleteAlarm(alarm) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to Add Alarm
        FloatingActionButton(
            onClick = {
                editingAlarm = null
                showAddDialog = true
            },
            containerColor = GoldPrimary,
            contentColor = Color(0xFF0F172A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_alarm_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة منبه")
        }
    }

    if (showAddDialog) {
        AlarmEditDialog(
            alarm = editingAlarm,
            sounds = sounds,
            onDismiss = {
                showAddDialog = false
                editingAlarm = null
            },
            onSave = { newAlarm ->
                viewModel.saveAlarm(newAlarm)
                showAddDialog = false
                editingAlarm = null
            }
        )
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var h = alarm.hour % 12
    if (h == 0) h = 12
    val amPm = if (alarm.hour < 12) "ص" else "م"
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", h, alarm.minute, amPm)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (alarm.isEnabled) GoldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeFormatted,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (alarm.isEnabled) GoldBright else TextMuted
                    )
                    Text(
                        text = alarm.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoldBright,
                        checkedTrackColor = Color(0xFF382C0E),
                        uncheckedThumbColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days & Sound summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alarm.soundName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${alarm.snoozeMinutes} د",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = GoldBright, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditDialog(
    alarm: AlarmEntity?,
    sounds: List<SoundEntity>,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    var title by remember { mutableStateOf(alarm?.title ?: "الاستيقاظ لصلاة الفجر") }
    var hour by remember { mutableIntStateOf(alarm?.hour ?: 4) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 30) }
    var isAm by remember { mutableStateOf(hour < 12) }
    var hour12 by remember {
        val h = hour % 12
        mutableIntStateOf(if (h == 0) 12 else h)
    }

    var snoozeMinutes by remember { mutableIntStateOf(alarm?.snoozeMinutes ?: 10) }
    var vibrate by remember { mutableStateOf(alarm?.vibrate ?: true) }

    val daysMap = listOf(
        Pair(6, "السبت"),
        Pair(0, "الأحد"),
        Pair(1, "الاثنين"),
        Pair(2, "الثلاثاء"),
        Pair(3, "الأربعاء"),
        Pair(4, "الخميس"),
        Pair(5, "الجمعة")
    )

    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            val initial = alarm?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
                ?: listOf(0, 1, 2, 3, 4, 5, 6)
            addAll(initial)
        }
    }

    var selectedSoundUri by remember { mutableStateOf(alarm?.soundUri ?: "raw/alarm_gentle") }
    var selectedSoundName by remember { mutableStateOf(alarm?.soundName ?: "رنين المنبه الهادئ") }
    var soundDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (alarm == null) "إضافة منبه جديد" else "تعديل المنبه",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم المنبه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick suggestions
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("استيقاظ", "قيام الليل", "صلاة الضحى", "العمل", "الدراسة").forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22D4AF37),
                            modifier = Modifier
                                .clickable { title = tag }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                color = GoldBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Time Selection (12h format + AM/PM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", hour12),
                        onValueChange = {
                            val v = it.toIntOrNull() ?: 1
                            hour12 = v.coerceIn(1, 12)
                        },
                        label = { Text("الساعة") },
                        modifier = Modifier.width(76.dp)
                    )

                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", minute),
                        onValueChange = {
                            val v = it.toIntOrNull() ?: 0
                            minute = v.coerceIn(0, 59)
                        },
                        label = { Text("الدقيقة") },
                        modifier = Modifier.width(76.dp)
                    )

                    Row {
                        FilterChip(
                            selected = isAm,
                            onClick = { isAm = true },
                            label = { Text("ص") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !isAm,
                            onClick = { isAm = false },
                            label = { Text("م") }
                        )
                    }
                }

                // Repeat Days
                Text("أيام التكرار:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysMap.forEach { (dayIndex, dayName) ->
                        val isSelected = selectedDays.contains(dayIndex)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    if (isSelected) selectedDays.remove(dayIndex)
                                    else selectedDays.add(dayIndex)
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Sound Selector
                ExposedDropdownMenuBox(
                    expanded = soundDropdownExpanded,
                    onExpandedChange = { soundDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSoundName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("صوت المنبه") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soundDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = soundDropdownExpanded,
                        onDismissRequest = { soundDropdownExpanded = false }
                    ) {
                        sounds.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.category})") },
                                onClick = {
                                    selectedSoundUri = s.filePath
                                    selectedSoundName = s.name
                                    soundDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Snooze & Vibrate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مدة الغفوة:", fontSize = 13.sp)
                    Row {
                        listOf(5, 10, 15).forEach { min ->
                            FilterChip(
                                selected = snoozeMinutes == min,
                                onClick = { snoozeMinutes = min },
                                label = { Text("$min د") },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الاهتزاز:", fontSize = 13.sp)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHour24 = if (isAm) {
                        if (hour12 == 12) 0 else hour12
                    } else {
                        if (hour12 == 12) 12 else hour12 + 12
                    }

                    val updatedAlarm = AlarmEntity(
                        id = alarm?.id ?: 0L,
                        title = title.ifBlank { "منبه" },
                        hour = finalHour24,
                        minute = minute,
                        daysOfWeek = selectedDays.joinToString(","),
                        isEnabled = true,
                        snoozeMinutes = snoozeMinutes,
                        vibrate = vibrate,
                        soundUri = selectedSoundUri,
                        soundName = selectedSoundName
                    )
                    onSave(updatedAlarm)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("حفظ المنبه", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
