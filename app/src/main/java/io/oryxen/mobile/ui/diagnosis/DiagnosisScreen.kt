package io.oryxen.mobile.ui.diagnosis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.DiagnosisRepository
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.data.remote.DiagnosisResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosisUiState(
    val diagnoses: List<DiagnosisResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class DiagnosisViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiagnosisUiState(loading = true))
    val state: StateFlow<DiagnosisUiState> = _state.asStateFlow()

    private val currentPlantId: String?
        get() = ApiProvider.instance.secureStorage.currentPlantId

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val plantId = currentPlantId
            if (plantId.isNullOrBlank()) {
                _state.value = DiagnosisUiState(
                    loading = false,
                    error = "No plant selected. Go to Dashboard and enter a Plant ID.",
                )
                return@launch
            }
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val diagnoses = DiagnosisRepository.forPlant(plantId)
                _state.value = DiagnosisUiState(diagnoses = diagnoses, loading = false)
            } catch (e: Exception) {
                _state.value = DiagnosisUiState(
                    loading = false,
                    error = e.message ?: "Failed to load diagnoses",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    onBack: () -> Unit,
    viewModel: DiagnosisViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Pest Diagnosis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Camera intent would launch here */ },
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture crop photo")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "AI Crop Health Diagnosis",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Analyze your plant's health with multimodal AI",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            when {
                state.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading diagnoses...",
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }
                }

                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                state.diagnoses.isEmpty() -> {
                    Text(
                        text = "No diagnoses yet. Tap the camera button to analyze a crop photo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.diagnoses) { diagnosis ->
                            DiagnosisCard(diagnosis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosisCard(d: DiagnosisResponse) {
    val pestColor = if (d.detectedPest == "None") {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = d.detectedPest,
                    fontWeight = FontWeight.Bold,
                    color = pestColor,
                )
                Text(
                    text = "${(d.confidenceScore * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = d.recommendation,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = d.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
