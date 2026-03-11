package com.sanghyuk.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanghyuk.domain.mood.usecase.DeleteAllMoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val deleteAllMoodsUseCase: DeleteAllMoodsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                notificationHour = hour,
                notificationMinute = minute,
            )
        }
    }

    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetAllRecords() {
        viewModelScope.launch {
            deleteAllMoodsUseCase()
            _uiState.update {
                it.copy(showResetDialog = false)
            }
        }
    }
}

data class SettingsUiState(
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 21,
    val notificationMinute: Int = 0,
    val showResetDialog: Boolean = false,
)