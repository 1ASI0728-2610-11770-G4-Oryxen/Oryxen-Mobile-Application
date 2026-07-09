package io.oryxen.mobile.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.AnalyticsRepository
import io.oryxen.mobile.data.remote.DashboardResponse
import io.oryxen.mobile.data.remote.PlantHealthSummaryDto
import io.oryxen.mobile.data.remote.PlantTrendResponse
import io.oryxen.mobile.data.remote.TrendPointDto
import io.oryxen.mobile.ui.theme.OryxenGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val dashboard: DashboardResponse? = null,
    val selectedPlantId: String? = null,
    val trends: PlantTrendResponse? = null,
    val selectedPeriod: String = "Daily", // Daily, Weekly, Monthly
    val loading: Boolean = false,
    val error: String? = null,
)

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val dash = AnalyticsRepository.getDashboard()
                _state.update { 
                    it.copy(
                        dashboard = dash, 
                        loading = false,
                        selectedPlantId = dash.plantSummaries.firstOrNull()?.plantId
                    ) 
                }
                dash.plantSummaries.firstOrNull()?.plantId?.let { loadTrends(it) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed to load dashboard") }
            }
        }
    }

    fun selectPlant(plantId: String) {
        _state.update { it.copy(selectedPlantId = plantId) }
        loadTrends(plantId)
    }

    fun setPeriod(period: String) {
        _state.update { it.copy(selectedPeriod = period) }
    }

    private fun loadTrends(plantId: String) {
        viewModelScope.launch {
            try {
                val trends = AnalyticsRepository.getPlantTrends(plantId)
                _state.update { it.copy(trends = trends) }
            } catch (e: Exception) {
                // Handle silently for now or show error
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "History & Trends",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.loading && state.dashboard == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            return
        }

        state.dashboard?.let { dash ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "Avg Health",
                    value = "${dash.avgHealthScore.toInt()}/100",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Avg Temp",
                    value = "${dash.avgTemperature.toInt()}°C",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Avg Hum",
                    value = "${dash.avgHumidity.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select Plant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dash.plantSummaries) { plant ->
                    PlantChip(
                        plant = plant,
                        isSelected = state.selectedPlantId == plant.plantId,
                        onClick = { viewModel.selectPlant(plant.plantId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            state.trends?.let { trends ->
                Text(
                    text = "Health Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Daily", "Weekly", "Monthly").forEach { period ->
                        TextButton(
                            onClick = { viewModel.setPeriod(period) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (state.selectedPeriod == period) OryxenGreen else Color.Gray
                            )
                        ) {
                            Text(period)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val currentPoints = when (state.selectedPeriod) {
                    "Daily" -> trends.daily
                    "Weekly" -> trends.weekly
                    "Monthly" -> trends.monthly
                    else -> emptyList()
                }

                LineChart(
                    points = currentPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } ?: run {
                if (state.selectedPlantId != null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlantChip(plant: PlantHealthSummaryDto, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) OryxenGreen else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = plant.plantName,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LineChart(points: List<TrendPointDto>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available for this period.")
        }
        return
    }

    val maxHealth = 100f
    val minHealth = 0f

    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height
        val spacePerPoint = width / (points.size.coerceAtLeast(2) - 1)

        val path = Path()

        points.forEachIndexed { index, point ->
            val x = index * spacePerPoint
            // health score ratio
            val ratio = (point.avgHealthScore.toFloat() - minHealth) / (maxHealth - minHealth)
            val y = height - (ratio * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            drawCircle(
                color = OryxenGreen,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = OryxenGreen,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
