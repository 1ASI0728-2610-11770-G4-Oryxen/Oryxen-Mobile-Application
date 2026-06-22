package io.oryxen.mobile.security

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface SessionEvent {
    data object Expired : SessionEvent
}

object SessionEventBus {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun emit(event: SessionEvent) {
        _events.tryEmit(event)
    }
}
