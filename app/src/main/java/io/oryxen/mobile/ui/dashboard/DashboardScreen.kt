package io.oryxen.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.TelemetryRepository
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.data.remote.TelemetryReading
import io.oryxen.mobile.domain.healthStatusOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val readings: List<TelemetryReading> = emptyList(),
    val error: String? = null,
    val sending: Boolean = false,
)

class DashboardViewModel : ViewModel() {
    private val provider = ApiProvider.instance

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private var pollingJob: Job? = null

    init {
        val plantId = provider.secureStorage.currentPlantId
        if (!plantId.isNullOrBlank()) {
            startPolling()
        }
    }

    fun setPlantId(plantId: String) {
        provider.secureStorage.currentPlantId = plantId.trim()
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun refresh() {
        val plantId = provider.secureStorage.currentPlantId ?: return
        try {
            val data = TelemetryRepository.forPlant(plantId)
            _state.update { it.copy(readings = data, error = null) }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Cannot reach the Oryxen API (10.0.2.2:5170).") }
        }
    }

    fun sendSample() {
        val plantId = provider.secureStorage.currentPlantId ?: return
        _state.update { it.copy(sending = true) }
        viewModelScope.launch {
            try {
                TelemetryRepository.sendSample(plantId)
                refresh()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Could not send the reading.") }
            } finally {
                _state.update { it.copy(sending = false) }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateDiagnosis: () -> Unit = {},
    onNavigatePlans: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateAnalytics: () -> Unit = {},
    onNavigateCommunity: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val latest = state.readings.firstOrNull()
    val provider = ApiProvider.instance
    val savedPlantId = provider.secureStorage.currentPlantId ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oryxen · ${provider.session.fullName ?: "Farmer"}") },
                actions = {
                    IconButton(onClick = onNavigateNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onNavigateAnalytics) {
                        Icon(Icons.Filled.Leaderboard, contentDescription = "Crop Analytics")
                    }
                    IconButton(onClick = onNavigateDiagnosis) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "AI Diagnosis")
                    }
                    IconButton(onClick = onNavigatePlans) {
                        Icon(Icons.Filled.CreditCard, contentDescription = "Plans & Billing")
                    }
                    IconButton(onClick = onNavigateCommunity) {
                        Icon(Icons.Filled.Groups, contentDescription = "Community")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                },
            )
        },
        floatingActionButton = {
            if (savedPlantId.isNotBlank()) {
                FloatingActionButton(onClick = { viewModel.sendSample() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Send test reading")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            PlantIdConfigCard(
                savedPlantId = savedPlantId,
                onSave = { viewModel.setPlantId(it) },
            )

            if (savedPlantId.isBlank()) {
                Text(
                    "Enter a Plant ID to start monitoring.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
                return@Scaffold
            }

            if (latest != null) {
                val status = healthStatusOf(latest.healthScore)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(status.color),
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text("${latest.healthScore} / 100", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("${status.label} · latest health score", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
            }

            if (state.readings.isEmpty()) {
                Text("No readings yet. Tap + to simulate a Sensor Lite reading.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.readings) { reading -> TelemetryRow(reading) }
                }
            }
        }
    }
}

@Composable
private fun PlantIdConfigCard(
    savedPlantId: String,
    onSave: (String) -> Unit,
) {
    var plantIdInput by remember { mutableStateOf(savedPlantId) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Plant ID",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = plantIdInput,
                onValueChange = { plantIdInput = it },
                label = { Text("Plant UUID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            TextButton(
                onClick = { onSave(plantIdInput) },
                enabled = plantIdInput.isNotBlank(),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("Connect")
            }
        }
    }
}

@Composable
private fun TelemetryRow(reading: TelemetryReading) {
    val status = healthStatusOf(reading.healthScore)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(status.color),
            )
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text("Health ${reading.healthScore} · ${status.label}", fontWeight = FontWeight.SemiBold, color = status.color)
                Text(
                    "Soil ${reading.soilMoisture}%  ·  Hum ${reading.humidity}%  ·  ${reading.temperature}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }
        }
    }
}
