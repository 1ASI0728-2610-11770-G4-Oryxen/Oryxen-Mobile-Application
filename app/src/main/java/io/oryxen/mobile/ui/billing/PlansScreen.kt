package io.oryxen.mobile.ui.billing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.BillingRepository
import io.oryxen.mobile.data.remote.PlanResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlansUiState(
    val plans: List<PlanResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val checkoutLoading: String? = null,
)

class PlansViewModel : ViewModel() {
    private val _state = MutableStateFlow(PlansUiState(loading = true))
    val state: StateFlow<PlansUiState> = _state.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val plans = BillingRepository.getPlans()
                _state.value = PlansUiState(plans = plans, loading = false)
            } catch (e: Exception) {
                _state.value = PlansUiState(
                    loading = false,
                    error = e.message ?: "Failed to load plans",
                )
            }
        }
    }

    fun upgrade(planId: String, onCheckoutUrl: (String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(checkoutLoading = planId)
            try {
                val result = BillingRepository.createCheckout(planId)
                onCheckoutUrl(result.checkoutUrl)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Checkout failed",
                    checkoutLoading = null,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onBack: () -> Unit,
    viewModel: PlansViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plans & Pricing") },
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
                                text = "Loading plans...",
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

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.plans) { plan ->
                            PlanCard(
                                plan = plan,
                                isLoading = state.checkoutLoading == plan.id,
                                onUpgrade = {
                                    viewModel.upgrade(plan.id) { url ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: PlanResponse,
    isLoading: Boolean,
    onUpgrade: () -> Unit,
) {
    val isPremium = plan.name.lowercase().contains("premium")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPremium) 4.dp else 2.dp,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${plan.currency} ${"%.2f".format(plan.price)} / month",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            val features = plan.features.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            features.forEach { feature ->
                Text(
                    text = "✓ $feature",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Button(
                onClick = onUpgrade,
                enabled = isPremium && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(if (isPremium) "Upgrade to Premium" else "Current Plan")
            }
        }
    }
}
