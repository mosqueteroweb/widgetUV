package com.hermes.widgetuv.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.model.UvHourlyPoint
import com.hermes.widgetuv.data.model.UvRiskLevel
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.ui.theme.WidgetUVTheme
import com.hermes.widgetuv.widget.UvGlanceWidget
import com.hermes.widgetuv.worker.UvScheduleManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure 7:00 AM and 2:00 PM schedules are active
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UvMainScreen() {
    val context = LocalContext.current
    val repository = remember { UvRepository(context) }
    val scope = rememberCoroutineScope()

    var uvInfo by remember { mutableStateOf<UvCurrentInfo?>(null) }
    var hourlyList by remember { mutableStateOf<List<UvHourlyPoint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData(forceGps: Boolean = false) {
        scope.launch {
            isLoading = true
            val (info, hourly) = repository.getCurrentUvInfo(forceGpsSync = forceGps, slotTag = "MANUAL")
            uvInfo = info
            hourlyList = hourly
            isLoading = false
            // Update widget instance
            UvGlanceWidget().updateAll(context)
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
            Toast.makeText(context, "Permiso no concedido. Usando ubicación por defecto.", Toast.LENGTH_SHORT).show()
            loadData(forceGps = false)
        }
    }

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
            loadData(forceGps = false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Índice UV", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
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

                    // Location Badge
                    Text(
                        text = "📍 ${info.locationName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // UV Circle Display
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f", info.currentUv),
                                fontSize = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = riskColor
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = riskColor
                            ) {
                                Text(
                                    text = risk.label.uppercase(Locale.getDefault()),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPS Policy Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⏱️ Política de Geolocalización Eficiente",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "El GPS solo se activa automáticamente 2 veces al día (a las 7:00 AM y 2:00 PM) para calcular la curva solar y ahorrar 100% de batería durante el resto del día.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(info.lastUpdated))
                            Text(
                                text = "Última sincronización GPS: $dateStr (${if (info.syncSlot.isNotEmpty()) info.syncSlot else "automática"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Health Recommendation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🛡️ Recomendación de Fotoprotección",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = risk.advice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pico máximo previsto hoy:")
                                Text(
                                    "${String.format(Locale.US, "%.1f", info.maxTodayUv)} UV (${info.maxTime})",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hourly Curve
                    if (hourlyList.isNotEmpty()) {
                        Text(
                            text = "Curva UV por Horas (Hoy)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(hourlyList) { item ->
                                val itemRisk = UvRiskLevel.fromValue(item.uv)
                                val itemColor = Color(android.graphics.Color.parseColor(itemRisk.hexColor))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = item.hourLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(itemColor)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.1f", item.uv),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = itemColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Manual Refresh Button
                    Button(
                        onClick = { loadData(forceGps = true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Forzar Lectura GPS y Refrescar")
                        }
                    }
                }
            }
        }
    }
}
