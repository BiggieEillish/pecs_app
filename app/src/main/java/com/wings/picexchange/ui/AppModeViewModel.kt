package com.wings.picexchange.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.settings.AppMode
import com.wings.picexchange.data.settings.PinStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppModeViewModel @Inject constructor(
    private val pinStore: PinStore,
) : ViewModel() {

    // Session-only: always LEARNER on a fresh launch, so the child-lock re-engages each cold start.
    private val _mode = MutableStateFlow(AppMode.LEARNER)
    val mode: StateFlow<AppMode> = _mode.asStateFlow()

    val hasPin: StateFlow<Boolean> =
        pinStore.hasPin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** First-run: set the PIN and enter Teacher mode immediately. */
    fun setPinAndUnlock(pin: String) {
        viewModelScope.launch {
            pinStore.setPin(pin)
            _mode.value = AppMode.TEACHER
        }
    }

    /** Verify the PIN; on success enter Teacher mode. Reports the result for error display. */
    fun unlock(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = pinStore.verifyPin(pin)
            if (ok) _mode.value = AppMode.TEACHER
            onResult(ok)
        }
    }

    fun lock() {
        _mode.value = AppMode.LEARNER
    }
}
