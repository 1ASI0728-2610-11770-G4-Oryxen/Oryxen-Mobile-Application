package io.oryxen.mobile.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.AnalyticsRepository
import io.oryxen.mobile.data.remote.DashboardResponse
import io.oryxen.mobile.data.remote.TrendPointDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val dashboard: DashboardResponse? = null,
    val dailyTrends: List<TrendPointDto> = emptyList(),
    val selectedPlantId: String? = null,
    val selectedPlantName: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState(loading = true))
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val dashboard = AnalyticsRepository.getDashboard()
                _state.update {
                    it.copy(dashboard = dashboard, loading = false)
                }
                val firstPlant = dashboard.plantSummaries.firstOrNull()
                if (firstPlant != null) {
                    loadTrends(firstPlant.plantId, firstPlant.plantName)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun loadTrends(plantId: String, plantName: String) {
        _state.update { it.copy(selectedPlantId = plantId, selectedPlantName = plantName) }
        viewModelScope.launch {
            try {
                val trends = AnalyticsRepository.getPlantTrends(plantId)
                _state.update { it.copy(dailyTrends = trends.daily) }
            } catch (e: Exception) {
                _state.update { it.copy(dailyTrends = emptyList()) }
            }
        }
    }
}

private fun healthColor(score: Double): Color = when {
    score >= 80 -> Color(0xFF22C55E)
    score >= 60 -> Color(0xFF3B82F6)
    score >= 30 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("Loading analytics...", modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }

                state.error != null -> {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadDashboard() }, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Retry")
                    }
                }

                state.dashboard == null -> {
                    Text("No analytics data available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                else -> {
                    val d = state.dashboard!!
                    Text("Dashboard Overview", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryCard("Total", "${d.totalPlants}", Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                        SummaryCard("Healthy", "${d.healthyPlants}", Color(0xFF22C55E), modifier = Modifier.weight(1f))
                        SummaryCard("Critical", "${d.criticalPlants}", Color(0xFFEF4444), modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryCard("Avg Health", "${d.avgHealthScore.toInt()}", healthColor(d.avgHealthScore), modifier = Modifier.weight(1f))
                        SummaryCard("Soil Moist", "${d.avgSoilMoisture.toInt()}%", Color(0xFF8BC34A), modifier = Modifier.weight(1f))
                        SummaryCard("Temp", "${d.avgTemperature.toInt()}°", Color(0xFFFF9800), modifier = Modifier.weight(1f))
                    }

                    if (d.plantSummaries.isNotEmpty()) {
                        Text("Plants", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(d.plantSummaries) { plant ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { viewModel.loadTrends(plant.plantId, plant.plantName) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (state.selectedPlantId == plant.plantId)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    when (plant.status) {
                                                        "healthy" -> Modifier.fillMaxSize()
                                                        else -> Modifier.fillMaxSize()
                                                    }
                                                ),
                                        )
                                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                            Text(plant.plantName, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${plant.plantType}  ·  Health ${plant.avgHealthScore.toInt()}  ·  ${plant.readingCount} readings",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (state.dailyTrends.isNotEmpty()) {
                            Text(
                                "Daily Trends · ${state.selectedPlantName}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                            )
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(state.dailyTrends) { point ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(point.label, fontWeight = FontWeight.Medium)
                                            Text(
                                                "Health ${point.avgHealthScore.toInt()}  ·  Soil ${point.avgSoilMoisture.toInt()}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = healthColor(point.avgHealthScore),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}
