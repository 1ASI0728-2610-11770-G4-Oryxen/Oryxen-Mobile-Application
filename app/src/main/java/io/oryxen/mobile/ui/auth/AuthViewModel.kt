package io.oryxen.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.oryxen.mobile.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

/**
 * Shared ViewModel for Sign In and Sign Up screens.
 * Delegates to [AuthRepository] for actual network calls.
 */
class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Please enter your email and password.") }
            return
        }
        execute(onSuccess) { AuthRepository.login(email.trim(), password) }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean,
        onSuccess: () -> Unit,
    ) {
        when {
            fullName.isBlank() -> {
                _state.update { it.copy(error = "Please enter your full name.") }
                return
            }
            email.isBlank() -> {
                _state.update { it.copy(error = "Please enter your email address.") }
                return
            }
            password.length < 8 -> {
                _state.update { it.copy(error = "Password must be at least 8 characters.") }
                return
            }
            password != confirmPassword -> {
                _state.update { it.copy(error = "Passwords do not match.") }
                return
            }
            !termsAccepted -> {
                _state.update { it.copy(error = "You must accept the Terms of Service.") }
                return
            }
        }
        execute(onSuccess) { AuthRepository.register(fullName.trim(), email.trim(), password) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun execute(onSuccess: () -> Unit, action: suspend () -> Unit) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                action()
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("401") == true -> "Invalid email or password."
                    e.message?.contains("409") == true -> "An account with this email already exists."
                    else -> e.message ?: "Connection failed. Please try again."
                }
                _state.update { it.copy(loading = false, error = msg) }
            }
        }
    }
}
