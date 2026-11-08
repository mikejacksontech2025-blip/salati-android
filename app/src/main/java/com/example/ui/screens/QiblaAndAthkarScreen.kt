package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SalatiViewModel
import com.example.ui.components.LuxuryCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDarkCard
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QiblaAndAthkarScreen(
    viewModel: SalatiViewModel,
    modifier: Modifier = Modifier
) {
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val city by viewModel.currentCity.collectAsState()
    val count by viewModel.tasbeehCount.collectAsState()
    val target by viewModel.tasbeehTarget.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: القبلة, 1: المسبحة والأذكار
    var selectedDhikrPhrase by remember { mutableStateOf("سبحان الله وبحمده") }

    val dhikrPresets = listOf(
        "سبحان الله وبحمده",
        "الحمد لله",
        "الله أكبر",
        "لا إله إلا الله وحده لا شريك له",
        "أستغفر الله وأتوب إليه",
        "اللهم صلّ وسلم على نبينا محمد",
        "لا حول ولا قوة إلا بالله"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("qibla_athkar_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldBright
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("اتجاه القبلة", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CompassCalibration, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("المسبحة والأذكار", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.TouchApp, contentDescription = null) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // Qibla Compass
                item {
                    SectionHeader(
                        title = "بوصلة القبلة المشرفة",
                        subtitle = "حساب دقيق لاتجاه الكعبة المشرفة بمكة المكرمة من موقعك",
                        icon = Icons.Default.Mosque
                    )
                }

                item {
                    QiblaCompassCard(
                        bearing = qiblaBearing,
                        cityName = city.nameArabic,
                        latitude = city.latitude,
                        longitude = city.longitude
                    )
                }

                item {
                    LuxuryCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "معلومات اتجاه القبلة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldBright
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• زاوية القبلة: ${String.format(Locale.getDefault(), "%.1f°", qiblaBearing)} من اتجاه الشمال الجغرافي.\n" +
                                        "• إحداثيات الكعبة: 21.4225° N, 39.8262° E بمكة المكرمة.\n" +
                                        "• وجه هاتفك بحيث يتطابق مؤشر السهم الذهبي مع رمز الكعبة المشرفة.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                // Tasbeeh & Athkar
                item {
                    SectionHeader(
                        title = "المسبحة الإلكترونية والأذكار",
                        subtitle = "«ألا بذكر الله تطمئن القلوب»",
                        icon = Icons.Default.TouchApp
                    )
                }

                item {
                    TasbeehCard(
                        count = count,
                        target = target,
                        selectedPhrase = selectedDhikrPhrase,
                        onIncrement = { viewModel.incrementTasbeeh() },
                        onReset = { viewModel.resetTasbeeh() },
                        onTargetChange = { viewModel.setTasbeehTarget(it) }
                    )
                }

                item {
                    Text(
                        text = "اختر صيغة الذكر:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dhikrPresets.forEach { phrase ->
                            FilterChip(
                                selected = selectedDhikrPhrase == phrase,
                                onClick = {
                                    selectedDhikrPhrase = phrase
                                    viewModel.resetTasbeeh()
                                },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    PostPrayerAthkarCard()
                }
            }
        }
    }
}

@Composable
fun QiblaCompassCard(
    bearing: Double,
    cityName: String,
    latitude: Double,
    longitude: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldPrimary, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "المدينة: $cityName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = String.format(Locale.getDefault(), "خط العرض: %.4f | خط الطول: %.4f", latitude, longitude),
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Compass Dial Canvas
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C1630))
                    .border(2.dp, GoldBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CompassDialCanvas(bearing = bearing.toFloat())

                // Kaaba Icon in Center
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "الكعبة",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x33D4AF37),
                modifier = Modifier.border(1.dp, GoldBright, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "زاوية القبلة: %.1f° درجة", bearing),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CompassDialCanvas(bearing: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 16f

        // Draw ticks
        for (i in 0 until 36) {
            val angle = Math.toRadians((i * 10).toDouble())
            val tickLen = if (i % 9 == 0) 18f else 10f
            val start = Offset(
                (center.x + (radius - tickLen) * Math.sin(angle)).toFloat(),
                (center.y - (radius - tickLen) * Math.cos(angle)).toFloat()
            )
            val end = Offset(
                (center.x + radius * Math.sin(angle)).toFloat(),
                (center.y - radius * Math.cos(angle)).toFloat()
            )
            drawLine(
                color = if (i % 9 == 0) Color(0xFFD4AF37) else Color(0x44D4AF37),
                start = start,
                end = end,
                strokeWidth = if (i % 9 == 0) 3f else 1.5f
            )
        }

        // Draw Qibla Pointer Arrow towards bearing angle
        val qiblaRad = Math.toRadians(bearing.toDouble())
        val arrowTip = Offset(
            (center.x + (radius - 12f) * Math.sin(qiblaRad)).toFloat(),
            (center.y - (radius - 12f) * Math.cos(qiblaRad)).toFloat()
        )
        val arrowBaseLeft = Offset(
            (center.x + (radius - 40f) * Math.sin(qiblaRad - 0.25)).toFloat(),
            (center.y - (radius - 40f) * Math.cos(qiblaRad - 0.25)).toFloat()
        )
        val arrowBaseRight = Offset(
            (center.x + (radius - 40f) * Math.sin(qiblaRad + 0.25)).toFloat(),
            (center.y - (radius - 40f) * Math.cos(qiblaRad + 0.25)).toFloat()
        )

        val path = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(arrowBaseLeft.x, arrowBaseLeft.y)
            lineTo(arrowBaseRight.x, arrowBaseRight.y)
            close()
        }
        drawPath(path, color = Color(0xFFF3C759))
    }
}

@Composable
fun TasbeehCard(
    count: Int,
    target: Int,
    selectedPhrase: String,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onTargetChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldPrimary, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Target pills (33, 100, مفتوح)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الهدف:", fontSize = 13.sp, color = TextLightGray)
                Row {
                    listOf(33, 100, 1000).forEach { t ->
                        FilterChip(
                            selected = target == t,
                            onClick = { onTargetChange(t) },
                            label = { Text(if (t == 1000) "حر" else "$t") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = selectedPhrase,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Big Clickable Tasbeeh Counter Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF233663), Color(0xFF0F1A35))
                        )
                    )
                    .border(3.dp, GoldBright, CircleShape)
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldBright
                    )
                    Text(
                        text = "من $target",
                        fontSize = 13.sp,
                        color = TextLightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x22D4AF37))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "تصفير", tint = GoldBright)
            }
        }
    }
}

@Composable
fun PostPrayerAthkarCard() {
    LuxuryCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "أذكار ما بعد الصلاة المكتوبة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright
            )
            Spacer(modifier = Modifier.height(10.dp))
            val athkar = listOf(
                "• «أستغفرُ الله، أستغفرُ الله، أستغفرُ الله، اللهم أنت السلام ومنك السلام، تباركت يا ذا الجلال والإكرام».",
                "• «لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير، اللهم لا مانع لما أعطيت، ولا معطي لما منعت، ولا ينفع ذا الجد منك الجد».",
                "• «سبحان الله (33)، والحمد لله (33)، والله أكبر (33)، تمام المائة: لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير».",
                "• قراءة آية الكرسي والمعوذتين والإخلاص بعد كل صلاة."
            )
            athkar.forEach { item ->
                Text(
                    text = item,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
