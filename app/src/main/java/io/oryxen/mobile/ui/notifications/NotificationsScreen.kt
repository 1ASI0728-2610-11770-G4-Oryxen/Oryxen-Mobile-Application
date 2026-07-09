package io.oryxen.mobile.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.NotificationRepository
import io.oryxen.mobile.data.remote.NotificationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class NotificationsViewModel : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState(loading = true))
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = NotificationsUiState(loading = true, error = null)
            try {
                val list = NotificationRepository.getAll()
                _state.value = NotificationsUiState(notifications = list, loading = false)
            } catch (e: Exception) {
                _state.value = NotificationsUiState(
                    loading = false,
                    error = e.message ?: "Failed to load notifications",
                )
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            try {
                NotificationRepository.markRead(id)
                val updated = _state.value.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                _state.value = _state.value.copy(notifications = updated)
            } catch (_: Exception) {
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading notifications...",
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

                state.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No notifications yet")
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarkRead = { viewModel.markRead(notification.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationResponse,
    onMarkRead: () -> Unit,
) {
    val severity = notificationSeverity(notification.type)
    val bgColor = if (!notification.isRead)
        severity.color.copy(alpha = 0.12f)
    else
        MaterialTheme.colorScheme.surface

    val contentDesc = when (notification.type) {
        1 -> "Alerta crítica: ${notification.title}"
        2 -> "Alerta de anomalías: ${notification.title}"
        3 -> "Recordatorio de riego: ${notification.title}"
        4 -> "Actualización del sistema: ${notification.title}"
        else -> notification.title
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDesc }
            .border(
                width = if (!notification.isRead) 2.dp else 1.dp,
                color = if (!notification.isRead) severity.color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(severity.color),
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = severity.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = severity.color,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!notification.isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onMarkRead,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text("Mark as read")
                    }
                }
            }
        }
    }
}

private data class NotificationSeverity(val color: Color, val label: String)

private fun notificationSeverity(type: Int): NotificationSeverity = when (type) {
    1 -> NotificationSeverity(
        color = Color(0xFFDC2626),
        label = "Critical Health",
    )
    2 -> NotificationSeverity(
        color = Color(0xFFF59E0B),
        label = "High Anomalies",
    )
    3 -> NotificationSeverity(
        color = Color(0xFF3B82F6),
        label = "Watering Reminder",
    )
    4 -> NotificationSeverity(
        color = Color(0xFF22C55E),
        label = "System Update",
    )
    else -> NotificationSeverity(
        color = Color(0xFF6B7280),
        label = "Notification",
    )
}

private fun formatDate(iso: String): String {
    return iso.take(10)
}
