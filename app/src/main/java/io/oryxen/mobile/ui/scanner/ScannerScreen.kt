package io.oryxen.mobile.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.PlantRepository
import io.oryxen.mobile.data.remote.PlantResponse
import io.oryxen.mobile.ui.theme.OryxenGreen
import io.oryxen.mobile.ui.theme.OryxenGreenSoft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── State Machine ───────────────────────────────────────────────────────

enum class ScannerStep { SCANNING, SELECT_PLANT, DONE }

data class ScannerUiState(
    val step: ScannerStep = ScannerStep.SCANNING,
    val loading: Boolean = false,
    val scannedDeviceId: String? = null,
    val plants: List<PlantResponse> = emptyList(),
    val selectedPlantName: String? = null,
    val error: String? = null,
)

class ScannerViewModel : ViewModel() {
    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    /** Simulates scanning a QR code. In production, this would use CameraX + ML Kit. */
    fun simulateScan() {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                // Simulate QR scan delay
                kotlinx.coroutines.delay(1500)
                val mockDeviceId = "SL-${System.currentTimeMillis().toString().takeLast(6)}"
                val plants = PlantRepository.getAll()
                _state.update {
                    it.copy(
                        step = ScannerStep.SELECT_PLANT,
                        loading = false,
                        scannedDeviceId = mockDeviceId,
                        plants = plants,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Scan failed")
                }
            }
        }
    }

    fun assignToPlant(plant: PlantResponse) {
        val deviceId = _state.value.scannedDeviceId ?: return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                // Real Sensor Lite binding: POST /api/v1/plants/{id}/sensor.
                PlantRepository.assignSensor(plant.id, deviceId)
                _state.update {
                    it.copy(
                        step = ScannerStep.DONE,
                        loading = false,
                        selectedPlantName = plant.name,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Could not link the sensor")
                }
            }
        }
    }
}

// ── Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ScannerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link Device") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.step) {
                ScannerStep.SCANNING -> ScanningStep(state, viewModel)
                ScannerStep.SELECT_PLANT -> SelectPlantStep(state, viewModel)
                ScannerStep.DONE -> DoneStep(state, onDone)
            }
        }
    }
}

@Composable
private fun ScanningStep(state: ScannerUiState, viewModel: ScannerViewModel) {
    Spacer(modifier = Modifier.height(40.dp))

    // ── QR Scanner Placeholder ──
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(OryxenGreenSoft),
        contentAlignment = Alignment.Center,
    ) {
        if (state.loading) {
            CircularProgressIndicator(color = OryxenGreen, modifier = Modifier.size(48.dp))
        } else {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = OryxenGreen,
                modifier = Modifier.size(72.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "Scan Sensor QR Code",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Point your camera at the QR code on your\nSensor Lite device to begin linking.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    state.error?.let {
        Spacer(modifier = Modifier.height(12.dp))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = { viewModel.simulateScan() },
        enabled = !state.loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OryxenGreen),
    ) {
        Text(
            if (state.loading) "Scanning..." else "Simulate QR Scan",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun SelectPlantStep(state: ScannerUiState, viewModel: ScannerViewModel) {
    Text(
        text = "Sensor Detected!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Device: ${state.scannedDeviceId}",
        style = MaterialTheme.typography.bodyMedium,
        color = OryxenGreen,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Select a plant to assign this sensor to:",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(20.dp))

    if (state.plants.isEmpty()) {
        Text(
            "No plants found. Create a plant first.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.plants, key = { it.id }) { plant ->
                Card(
                    onClick = { viewModel.assignToPlant(plant) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val status = io.oryxen.mobile.domain.healthStatusOf(plant.healthScore)
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OryxenGreenSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                tint = OryxenGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                plant.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "${plant.healthScore}% · ${status.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = status.color,
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.loading) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = OryxenGreen, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun DoneStep(state: ScannerUiState, onDone: () -> Unit) {
    Spacer(modifier = Modifier.height(60.dp))

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(OryxenGreenSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = OryxenGreen,
            modifier = Modifier.size(48.dp),
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Sensor Linked!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Device ${state.scannedDeviceId} has been\nassigned to ${state.selectedPlantName}.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onDone,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OryxenGreen),
    ) {
        Text("Back to Dashboard", style = MaterialTheme.typography.labelLarge)
    }
}
