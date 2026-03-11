package com.sanghyuk.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.model.MoodType
import com.sanghyuk.domain.mood.usecase.GetTodayMoodUseCase
import com.sanghyuk.domain.mood.usecase.SaveMoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayMoodUseCase: GetTodayMoodUseCase,
    private val saveMoodUseCase: SaveMoodUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayMood()
    }

    fun onMoodSelected(moodType: MoodType) {
        viewModelScope.launch {
            saveMoodUseCase(MoodEntry(date = LocalDate.now(), moodType = moodType))
            _uiState.update { current ->
                current.copy(selectedMood = moodType, isLoading = false)
            }
        }
    }

    private fun loadTodayMood() {
        viewModelScope.launch {
            val todayMood = getTodayMoodUseCase()?.moodType
            _uiState.update { current ->
                current.copy(selectedMood = todayMood, isLoading = false)
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedMood: MoodType? = null,
)
