package io.oryxen.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.AnalyticsRepository
import io.oryxen.mobile.data.NotificationRepository
import io.oryxen.mobile.data.PlantRepository
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.data.remote.DashboardResponse
import io.oryxen.mobile.data.remote.NotificationResponse
import io.oryxen.mobile.data.remote.PlantResponse
import io.oryxen.mobile.domain.healthStatusOf
import io.oryxen.mobile.ui.theme.OryxenGreen
import io.oryxen.mobile.ui.theme.OryxenGreenSoft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── ViewModel ───────────────────────────────────────────────────────────

data class DashboardUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val userName: String = "Farmer",
    val dashboard: DashboardResponse? = null,
    val featuredPlant: PlantResponse? = null,
    val recentNotifications: List<NotificationResponse> = emptyList(),
)

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState(loading = true))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val name = ApiProvider.instance.session.fullName ?: "Farmer"
                val dashboard = AnalyticsRepository.getDashboard()
                val plant = try { PlantRepository.getAll().firstOrNull() } catch (_: Exception) { null }
                val notifs = try { NotificationRepository.getAll().take(3) } catch (_: Exception) { emptyList() }

                _state.update {
                    it.copy(
                        loading = false,
                        userName = name.split(" ").first(),
                        dashboard = dashboard,
                        featuredPlant = plant,
                        recentNotifications = notifs,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Failed to load dashboard")
                }
            }
        }
    }
}

// ── Screen ──────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    onNavigateNotifications: () -> Unit = {},
    onNavigatePlantDetail: (String) -> Unit = {},
    onNavigateChatbot: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OryxenGreen)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { viewModel.load() }) { Text("Retry") }
                    }
                }
            }
            else -> DashboardContent(state, onNavigateNotifications, onNavigatePlantDetail, onNavigateChatbot)
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onNavigateNotifications: () -> Unit,
    onNavigatePlantDetail: (String) -> Unit,
    onNavigateChatbot: () -> Unit,
) {
    val dashboard = state.dashboard

    // ── Header ──
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Hello, ${state.userName}!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = if ((dashboard?.criticalPlants ?: 0) > 0)
                    "Some plants need attention"
                else
                    "Your plants are looking healthy today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OryxenGreenSoft)
                    .clickable { onNavigateChatbot() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "AI Assistant",
                    tint = OryxenGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OryxenGreenSoft)
                    .clickable { onNavigateNotifications() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = OryxenGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Stats Grid 2×2 ──
    if (dashboard != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Eco,
                value = "${dashboard.totalPlants}",
                label = "Total Plants",
                iconTint = OryxenGreen,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Warning,
                value = "${dashboard.criticalPlants + dashboard.warningPlants}",
                label = "Active Alerts",
                iconTint = Color(0xFFF59E0B),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Opacity,
                value = "${dashboard.avgHumidity.toInt()}%",
                label = "Avg Humidity",
                iconTint = Color(0xFF3B82F6),
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DeviceThermostat,
                value = "${dashboard.avgTemperature.toInt()}°C",
                label = "Avg Temp",
                iconTint = Color(0xFFEF4444),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Featured Plant Card ──
    state.featuredPlant?.let { plant ->
        FeaturedPlantCard(
            plant = plant,
            onClick = { onNavigatePlantDetail(plant.id) },
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Recent Activity ──
    if (state.recentNotifications.isNotEmpty()) {
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        state.recentNotifications.forEach { notif ->
            ActivityItem(notif)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Bottom spacer for FAB clearance
    Spacer(modifier = Modifier.height(80.dp))
}

// ── Components ──────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeaturedPlantCard(
    plant: PlantResponse,
    onClick: () -> Unit,
) {
    val status = healthStatusOf(plant.healthScore)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OryxenGreenSoft),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Plant avatar placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(OryxenGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Eco,
                    contentDescription = null,
                    tint = OryxenGreen,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Health ${plant.healthScore}% · ${status.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = status.color,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiniMetric("Humidity", "—")
                    MiniMetric("Light", "—")
                    MiniMetric("Status", status.label)
                }
            }

            Button(
                onClick = { /* TODO: water */ },
                colors = ButtonDefaults.buttonColors(containerColor = OryxenGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp),
            ) {
                Text("Water Now", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActivityItem(notification: NotificationResponse) {
    val iconColor = when (notification.type) {
        1 -> Color(0xFFDC2626) // Critical
        2 -> Color(0xFFF59E0B) // Anomaly
        3 -> Color(0xFF3B82F6) // Watering
        else -> OryxenGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = when (notification.type) {
                    1 -> Icons.Filled.Warning
                    3 -> Icons.Filled.WaterDrop
                    else -> Icons.Filled.Notifications
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = notification.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
