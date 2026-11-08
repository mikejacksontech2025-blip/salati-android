package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.SoundEntity
import com.example.prayer.PrayerType
import com.example.ui.SalatiViewModel
import com.example.ui.components.LuxuryCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SoundLibraryScreen(
    viewModel: SalatiViewModel,
    modifier: Modifier = Modifier
) {
    val sounds by viewModel.sounds.collectAsState()
    val playingId by viewModel.playingSoundId.collectAsState()
    val currentVolume by viewModel.adhanVolume.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var soundToEdit by remember { mutableStateOf<SoundEntity?>(null) }
    var soundToTrim by remember { mutableStateOf<SoundEntity?>(null) }
    var soundToAssign by remember { mutableStateOf<SoundEntity?>(null) }

    // Audio file picker launcher
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    val filteredSounds = remember(sounds, selectedCategoryFilter) {
        if (selectedCategoryFilter == "الكل") sounds
        else sounds.filter { it.category == selectedCategoryFilter }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sound_library_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionHeader(
                    title = "مكتبة الأذان والأصوات",
                    subtitle = "أصوات أذان الحرمين وتنبيهات يمكنك تخصيصها وتعيينها",
                    icon = Icons.Default.LibraryMusic
                )
            }

            // Volume Control Card
            item {
                VolumeControlCard(
                    volume = currentVolume,
                    onVolumeChanged = { viewModel.setVolume(it) }
                )
            }

            // Filter Chips + Import Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("الكل", "أذان", "تنبيه", "منبه").forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { audioPicker.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استيراد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sound List
            items(filteredSounds, key = { it.id }) { sound ->
                val isPlaying = playingId == sound.id
                SoundItemCard(
                    sound = sound,
                    isPlaying = isPlaying,
                    onTogglePlay = { viewModel.togglePlaySoundPreview(sound) },
                    onEdit = { soundToEdit = sound },
                    onTrim = { soundToTrim = sound },
                    onAssign = { soundToAssign = sound },
                    onDelete = { viewModel.deleteSound(sound) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Import Dialog
    if (showImportConfirmDialog && pendingImportUri != null) {
        ImportSoundDialog(
            uri = pendingImportUri!!,
            onDismiss = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            onConfirm = { name, category ->
                viewModel.importSoundFromUri(pendingImportUri!!, name, category)
                showImportConfirmDialog = false
                pendingImportUri = null
            }
        )
    }

    // Edit Name/Category Dialog
    soundToEdit?.let { s ->
        EditSoundDialog(
            sound = s,
            onDismiss = { soundToEdit = null },
            onSave = { updated ->
                viewModel.updateSound(updated)
                soundToEdit = null
            }
        )
    }

    // Trim Dialog
    soundToTrim?.let { s ->
        TrimSoundDialog(
            sound = s,
            onDismiss = { soundToTrim = null },
            onSave = { startSec, endSec ->
                val updated = s.copy(trimStartMs = startSec * 1000, trimEndMs = endSec * 1000)
                viewModel.updateSound(updated)
                soundToTrim = null
            }
        )
    }

    // Assign to Prayer / Alarm Dialog
    soundToAssign?.let { s ->
        AssignSoundDialog(
            sound = s,
            onDismiss = { soundToAssign = null },
            onAssignPrayer = { prayer, isFull ->
                viewModel.setPrayerSound(prayer, s.filePath, isFull)
                soundToAssign = null
            },
            onAssignAllPrayers = { isFull ->
                viewModel.setAllPrayersSound(s.filePath, isFull)
                soundToAssign = null
            }
        )
    }
}

@Composable
fun VolumeControlCard(
    volume: Float,
    onVolumeChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = GoldBright, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مستوى صوت الأذان والتنبيهات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text("${(volume * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GoldBright)
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChanged,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = GoldBright,
                    activeTrackColor = GoldPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SoundItemCard(
    sound: SoundEntity,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onEdit: () -> Unit,
    onTrim: () -> Unit,
    onAssign: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPlaying) GoldBright else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF16254A) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) GoldPrimary else Color(0x22D4AF37))
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying) Color(0xFF0F172A) else GoldBright,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = sound.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) GoldBright else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22D4AF37)
                        ) {
                            Text(
                                text = sound.category,
                                fontSize = 10.sp,
                                color = GoldBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${sound.durationSec} ثانية",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (sound.isBuiltIn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("• أصلي", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            // Action Buttons
            Row {
                IconButton(onClick = onAssign, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Assignment, contentDescription = "تعيين", tint = GoldBright, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onTrim, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCut, contentDescription = "قص", tint = GoldBright, modifier = Modifier.size(18.dp))
                }
                if (!sound.isBuiltIn) {
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

@Composable
fun ImportSoundDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("أذان مخصص") }
    var category by remember { mutableStateOf("أذان") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استيراد ملف صوتي جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تم اختيار ملف من ذاكرة الهاتف. سيتم نسخه محلياً داخل التطبيق للاستخدام الدائم دون إنترنت.", fontSize = 13.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصوت") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("أذان", "تنبيه", "منبه").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, category) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("حفظ في المكتبة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun EditSoundDialog(
    sound: SoundEntity,
    onDismiss: () -> Unit,
    onSave: (SoundEntity) -> Unit
) {
    var name by remember { mutableStateOf(sound.name) }
    var category by remember { mutableStateOf(sound.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل اسم وتصنيف الصوت", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("أذان", "تنبيه", "منبه").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(sound.copy(name = name, category = category)) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun TrimSoundDialog(
    sound: SoundEntity,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var startSec by remember { mutableIntStateOf(sound.trimStartMs / 1000) }
    var endSec by remember { mutableIntStateOf(if (sound.trimEndMs > 0) sound.trimEndMs / 1000 else sound.durationSec) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("معاينة وقص الصوت", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "حدد بداية ونهاية تشغيل الصوت عند انطلاق الأذان أو التنبيه (المدة الإجمالية: ${sound.durationSec} ثانية):",
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("بداية التشغيل (ثانية):", fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (startSec > 0) startSec-- }) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Text("$startSec", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (startSec < endSec - 1) startSec++ }) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نهاية التشغيل (ثانية):", fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (endSec > startSec + 1) endSec-- }) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Text("$endSec", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (endSec < sound.durationSec) endSec++ }) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(startSec, endSec) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A))
            ) {
                Text("حفظ التخصيص", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun AssignSoundDialog(
    sound: SoundEntity,
    onDismiss: () -> Unit,
    onAssignPrayer: (PrayerType, Boolean) -> Unit,
    onAssignAllPrayers: (Boolean) -> Unit
) {
    var isFullAdhan by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعيين: ${sound.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("اختر الصلاة التي ترغب بتعيين هذا الصوت لها:", fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نوع التنبيه:", fontSize = 12.sp)
                    Row {
                        FilterChip(
                            selected = isFullAdhan,
                            onClick = { isFullAdhan = true },
                            label = { Text("أذان كامل") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !isFullAdhan,
                            onClick = { isFullAdhan = false },
                            label = { Text("تنبيه قصير") }
                        )
                    }
                }

                Button(
                    onClick = { onAssignAllPrayers(isFullAdhan) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تعيين لجميع الصلوات الخمس", fontWeight = FontWeight.Bold)
                }

                PrayerType.mainPrayers.forEach { prayer ->
                    OutlinedButton(
                        onClick = { onAssignPrayer(prayer, isFullAdhan) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تعيين لصلاة ${prayer.arabicName}")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
