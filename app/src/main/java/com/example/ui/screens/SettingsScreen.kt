package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.CalculationMethod
import com.example.prayer.CityLocation
import com.example.prayer.CityLocations
import com.example.prayer.JuristicMethod
import com.example.ui.SalatiViewModel
import com.example.ui.components.LuxuryCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SalatiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val city by viewModel.currentCity.collectAsState()
    val calcMethod by viewModel.calculationMethod.collectAsState()
    val juristicMethod by viewModel.juristicMethod.collectAsState()
    val volume by viewModel.adhanVolume.collectAsState()
    val masterAdhan by viewModel.masterAdhanEnabled.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()

    var showCityPickerDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectLocationFromGps(context) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "إذن الموقع مطلوب للتحديد التلقائي", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "إعدادات التطبيق والموقع",
                subtitle = "تخصيص طرق الحساب والموقع والصوت",
                icon = Icons.Default.LocationOn
            )
        }

        // 1. Location Card
        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "المدينة الحالية: ${city.nameArabic}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${city.countryArabic} | ${city.timeZoneId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0F172A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تحديد عبر GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCityPickerDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("اختيار من القائمة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Calculation Methods
        item {
            SectionHeader(
                title = "طريقة الحساب والمذهب الفقهي",
                icon = Icons.Default.Calculate
            )
        }

        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "طريقة حساب مواقيت الصلاة:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    var calcDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = calcDropdownExpanded,
                        onExpandedChange = { calcDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = calcMethod.titleArabic,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calcDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = calcDropdownExpanded,
                            onDismissRequest = { calcDropdownExpanded = false }
                        ) {
                            CalculationMethod.values().forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.titleArabic) },
                                    onClick = {
                                        viewModel.setCalculationMethod(m)
                                        calcDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مذهب صلاة العصر (حساب الظل):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    var juristicExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = juristicExpanded,
                        onExpandedChange = { juristicExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = juristicMethod.titleArabic,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = juristicExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = juristicExpanded,
                            onDismissRequest = { juristicExpanded = false }
                        ) {
                            JuristicMethod.values().forEach { j ->
                                DropdownMenuItem(
                                    text = { Text(j.titleArabic) },
                                    onClick = {
                                        viewModel.setJuristicMethod(j)
                                        juristicExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Audio & Volume
        item {
            SectionHeader(
                title = "مستوى الصوت والمنبهات",
                icon = Icons.Default.VolumeUp
            )
        }

        item {
            LuxuryCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تشغيل جميع منبهات الأذان",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = masterAdhan,
                            onCheckedChange = { viewModel.toggleMasterAdhan() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مستوى الصوت: ${(volume * 100).toInt()}%", fontSize = 13.sp)
                    }

                    Slider(
                        value = volume,
                        onValueChange = { viewModel.setVolume(it) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = GoldBright, activeTrackColor = GoldPrimary)
                    )
                }
            }
        }

        // 4. Appearance Mode
        item {
            SectionHeader(
                title = "مظهر التطبيق",
                icon = Icons.Default.ColorLens
            )
        }

        item {
            LuxuryCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الوضع الليلي والنهاري:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row {
                        FilterChip(
                            selected = darkMode == "DARK",
                            onClick = { viewModel.setDarkMode("DARK") },
                            label = { Text("داكن") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        FilterChip(
                            selected = darkMode == "LIGHT",
                            onClick = { viewModel.setDarkMode("LIGHT") },
                            label = { Text("نهاري") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        // 5. Signature and Official Branding Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldBright, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B36))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مشروع تطبيق صَلَاتِي – SALATI",
                        color = GoldBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "من تطوير وتوقيع SALAH",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "«صلاتي صلاح لحياتي»",
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "«صلاتك في وقتها، وطمأنينة لقلبك وحياتك»",
                        color = TextLightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تطبيق إسلامي كامل، مجاني لوجه الله تعالى بدون إنترنت وبدون إعلانات وبدون اشتراكات.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showCityPickerDialog) {
        CityPickerDialog(
            cities = CityLocations.predefinedCities,
            onDismiss = { showCityPickerDialog = false },
            onSelect = { selected ->
                viewModel.selectCity(selected)
                showCityPickerDialog = false
            }
        )
    }
}

@Composable
fun CityPickerDialog(
    cities: List<CityLocation>,
    onDismiss: () -> Unit,
    onSelect: (CityLocation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) cities
        else cities.filter { it.nameArabic.contains(searchQuery) || it.countryArabic.contains(searchQuery) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اختر مدينتك", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث عن مدينة أو دولة...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(filtered) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(city) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(city.nameArabic, fontWeight = FontWeight.Bold)
                            Text(city.countryArabic, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
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
