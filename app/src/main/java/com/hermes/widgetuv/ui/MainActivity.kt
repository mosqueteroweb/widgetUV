package com.hermes.widgetuv.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hermes.widgetuv.R
import com.hermes.widgetuv.data.LanguageManager
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.model.UvHourlyPoint
import com.hermes.widgetuv.data.model.UvRiskLevel
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.ui.theme.WidgetUVTheme
import com.hermes.widgetuv.worker.UvScheduleManager
import com.hermes.widgetuv.worker.updateAllGlanceWidgets
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UvScheduleManager.setupAllSchedules(applicationContext)

        setContent {
            WidgetUVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UvMainScreen()
                }
            }
        }
    }
}

data class RecommendationCardData(
    val icon: String,
    val title: String,
    val description: String,
    val detailBadge: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UvMainScreen() {
    val context = LocalContext.current
    val repository = remember { UvRepository(context) }
    val scope = rememberCoroutineScope()
    val hourlyListState = rememberLazyListState()

    var isEnglish by remember { mutableStateOf(LanguageManager.isEnglish(context)) }
    var uvInfo by remember { mutableStateOf<UvCurrentInfo?>(null) }
    var hourlyList by remember { mutableStateOf<List<UvHourlyPoint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedRecTab by remember { mutableIntStateOf(0) }

    fun loadData(forceGps: Boolean = true) {
        scope.launch {
            isLoading = true
            val (info, hourly) = repository.getCurrentUvInfo(forceGpsSync = forceGps, slotTag = "APP_OPEN")
            uvInfo = info
            hourlyList = hourly
            isLoading = false
            updateAllGlanceWidgets(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            loadData(forceGps = true)
        } else {
            val msg = if (isEnglish) "Permission denied. Using saved location." else "Permiso no concedido. Usando provincia guardada."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            loadData(forceGps = false)
        }
    }

    // Auto-update GPS & Province on App Launch
    LaunchedEffect(Unit) {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarse) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            loadData(forceGps = true)
        }
    }

    // Center hourly curve on current hour automatically
    LaunchedEffect(hourlyList) {
        if (hourlyList.isNotEmpty()) {
            val currentHour = LocalDateTime.now().hour
            val currentHourIndex = hourlyList.indexOfFirst {
                it.hourLabel.startsWith(String.format(Locale.US, "%02d", currentHour))
            }
            if (currentHourIndex >= 0) {
                val targetScrollIndex = (currentHourIndex - 2).coerceAtLeast(0)
                hourlyListState.animateScrollToItem(targetScrollIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEnglish) "UV Index" else "Índice UV",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Flag Toggle Button for Language Switch (ES <-> EN)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                isEnglish = LanguageManager.toggleLanguage(context)
                                scope.launch {
                                    updateAllGlanceWidgets(context)
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "🇬🇧 EN" else "🇪🇸 ES",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading && uvInfo == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                uvInfo?.let { info ->
                    val risk = info.riskLevel
                    val riskColor = Color(android.graphics.Color.parseColor(risk.hexColor))
                    val currentHour = LocalDateTime.now().hour

                    // 1. Clickable Province Location Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { loadData(forceGps = true) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "📍 ${info.locationName}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLoading) "⏳" else "🔄",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // 2. UV Circle Display
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f", info.currentUv),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = riskColor
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = riskColor
                            ) {
                                Text(
                                    text = risk.getLabel(isEnglish).uppercase(Locale.getDefault()),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Peak Info Card (Value under "Peak UV today:")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "☀️ Peak UV today:" else "☀️ Pico máximo hoy:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                val peakText = if (isEnglish) {
                                    "${String.format(Locale.US, "%.1f", info.maxTodayUv)} UV at ${info.maxTime}"
                                } else {
                                    "${String.format(Locale.US, "%.1f", info.maxTodayUv)} UV a las ${info.maxTime}"
                                }
                                Text(
                                    text = peakText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Hourly Curve
                    if (hourlyList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEnglish) "Hourly Forecast" else "Curva Horaria",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnglish) "7:00 to 22:00" else "7:00 a 22:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            state = hourlyListState,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(hourlyList) { _, item ->
                                val itemRisk = UvRiskLevel.fromValue(item.uv)
                                val itemColor = Color(android.graphics.Color.parseColor(itemRisk.hexColor))
                                val isThisHour = item.hourLabel.startsWith(String.format(Locale.US, "%02d", currentHour))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isThisHour) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .then(
                                            if (isThisHour) Modifier.border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            else Modifier
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = item.hourLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isThisHour) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isThisHour) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    if (isThisHour) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = if (isEnglish) "NOW" else "AHORA",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.1f", item.uv),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = itemColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Interactive Recommendation Carousel (Fully Bilingual)
                    val recCards = remember(info.currentUv, isEnglish) {
                        if (isEnglish) {
                            listOf(
                                RecommendationCardData(
                                    icon = "🛡️",
                                    title = "Personalized Sun Protection",
                                    description = risk.adviceEn,
                                    detailBadge = when (risk) {
                                        UvRiskLevel.LOW -> "Minimal Risk"
                                        UvRiskLevel.MODERATE -> "Moderate Protection"
                                        UvRiskLevel.HIGH -> "High Protection"
                                        UvRiskLevel.VERY_HIGH -> "Very High Protection"
                                        UvRiskLevel.EXTREME -> "Maximum Alert"
                                    }
                                ),
                                RecommendationCardData(
                                    icon = "🧴",
                                    title = "Sun Protection Factor (SPF)",
                                    description = when (risk) {
                                        UvRiskLevel.LOW -> "SPF 15+ recommended for fair skin during prolonged exposure."
                                        UvRiskLevel.MODERATE -> "Apply SPF 30 or 50 every 2 hours or after swimming."
                                        UvRiskLevel.HIGH -> "SPF 50+ essential, along with a wide-brimmed hat and sunglasses."
                                        UvRiskLevel.VERY_HIGH, UvRiskLevel.EXTREME -> "Broad-spectrum SPF 50+ (UVA/UVB) and UV-protective shirt required."
                                    },
                                    detailBadge = when (risk) {
                                        UvRiskLevel.LOW -> "SPF 15+"
                                        UvRiskLevel.MODERATE -> "SPF 30"
                                        else -> "SPF 50+"
                                    }
                                ),
                                RecommendationCardData(
                                    icon = "☀️",
                                    title = "Vitamin D Synthesis",
                                    description = when {
                                        info.currentUv < 1.0 -> "Low UV: 30-40+ minutes of outdoor exposure needed for daily Vitamin D synthesis."
                                        info.currentUv < 6.0 -> "Optimal UV: 10 to 15 minutes of sun on face and arms provides the full daily dose."
                                        else -> "Caution: Daily synthesis achieved in under 8 minutes. Avoid direct prolonged exposure to prevent cellular damage."
                                    },
                                    detailBadge = "Bone & Immune Health"
                                ),
                                RecommendationCardData(
                                    icon = "👁️",
                                    title = "Eye Care & Sensitive Skin",
                                    description = "UV radiation accelerates photoaging and cataracts. Always wear certified UV400 sunglasses and protect young children.",
                                    detailBadge = "UV400 Filter"
                                )
                            )
                        } else {
                            listOf(
                                RecommendationCardData(
                                    icon = "🛡️",
                                    title = "Fotoprotección Personalizada",
                                    description = risk.adviceEs,
                                    detailBadge = when (risk) {
                                        UvRiskLevel.LOW -> "Riesgo Mínimo"
                                        UvRiskLevel.MODERATE -> "Protección Moderada"
                                        UvRiskLevel.HIGH -> "Protección Alta"
                                        UvRiskLevel.VERY_HIGH -> "Protección Muy Alta"
                                        UvRiskLevel.EXTREME -> "Alerta Máxima"
                                    }
                                ),
                                RecommendationCardData(
                                    icon = "🧴",
                                    title = "Factor Solar (FPS)",
                                    description = when (risk) {
                                        UvRiskLevel.LOW -> "FPS 15+ recomendado en pieles claras si la exposición es prolongada."
                                        UvRiskLevel.MODERATE -> "Usa FPS 30 o 50 y reaplica cada 2 horas o tras bañarte."
                                        UvRiskLevel.HIGH -> "Indispensable FPS 50+, sombrero de ala ancha y gafas de sol."
                                        UvRiskLevel.VERY_HIGH, UvRiskLevel.EXTREME -> "FPS 50+ de amplio espectro (UVA/UVB) y camiseta con protección UV."
                                    },
                                    detailBadge = when (risk) {
                                        UvRiskLevel.LOW -> "FPS 15+"
                                        UvRiskLevel.MODERATE -> "FPS 30"
                                        else -> "FPS 50+"
                                    }
                                ),
                                RecommendationCardData(
                                    icon = "☀️",
                                    title = "Síntesis de Vitamina D",
                                    description = when {
                                        info.currentUv < 1.0 -> "Radiación baja: se requieren más de 30-40 min al aire libre para síntesis de Vitamina D."
                                        info.currentUv < 6.0 -> "Nivel óptimo: 10 a 15 minutos de sol en brazos y rostro bastan para la dosis diaria."
                                        else -> "Atención: La síntesis se logra en menos de 8 min. Evita el sol directo para no dañar el ADN celular."
                                    },
                                    detailBadge = "Salud Ósea e Inmune"
                                ),
                                RecommendationCardData(
                                    icon = "👁️",
                                    title = "Ojos y Piel Sensible",
                                    description = "La radiación UV acelera el fotoenvejecimiento y cataratas. Usa gafas homologadas con filtro UV400 y extrema cuidado en niños.",
                                    detailBadge = "Filtro UV400"
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEnglish) "Health Recommendations" else "Recomendaciones de Salud",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    recCards.indices.forEach { index ->
                                        val isSelected = selectedRecTab == index
                                        Box(
                                            modifier = Modifier
                                                .size(if (isSelected) 8.dp else 5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val currentCard = recCards[selectedRecTab]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentCard.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentCard.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = currentCard.detailBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentCard.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        selectedRecTab = if (selectedRecTab > 0) selectedRecTab - 1 else recCards.size - 1
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (isEnglish) "◀ Previous" else "◀ Anterior",
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = "${selectedRecTab + 1} / ${recCards.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                TextButton(
                                    onClick = {
                                        selectedRecTab = (selectedRecTab + 1) % recCards.size
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = if (isEnglish) "Next ▶" else "Siguiente ▶",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Action Button
                    Button(
                        onClick = { loadData(forceGps = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📍", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Force GPS Sync & Refresh" else "Forzar Lectura GPS y Actualizar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtle GPS schedule hint
                    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(info.lastUpdated))
                    val syncText = if (isEnglish) {
                        "Synced: $dateStr · Auto-GPS: 7:00 AM / 2:00 PM"
                    } else {
                        "Sincronizado: $dateStr · Auto-GPS: 7:00 AM / 2:00 PM"
                    }
                    Text(
                        text = syncText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. Mosqueteroweb Authorship Footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mosqueteroweb),
                            contentDescription = "Mosqueteroweb",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEnglish) {
                                "Widget UV v1.2.5 • By Mosqueteroweb • Open Source (GPLv3)"
                            } else {
                                "Widget UV v1.2.5 • Por Mosqueteroweb • Código Libre (GPLv3)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
