package com.sanghyuk.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.model.MoodType
import com.sanghyuk.domain.mood.usecase.GetMoodEntriesUseCase
import com.sanghyuk.domain.mood.usecase.GetTodayMoodUseCase
import com.sanghyuk.domain.mood.usecase.SaveMoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayMoodUseCase: GetTodayMoodUseCase,
    private val getMoodEntriesUseCase: GetMoodEntriesUseCase,
    private val saveMoodUseCase: SaveMoodUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onMoodSelected(moodType: MoodType) {
        viewModelScope.launch {
            saveMoodUseCase(MoodEntry(date = LocalDate.now(), moodType = moodType))
            refreshState()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshState()
        }
    }

    private suspend fun refreshState() {
        val todayMood = getTodayMoodUseCase()?.moodType
        val recentEntries = getMoodEntriesUseCase()
            .associateBy { it.date }
            .let { entriesByDate ->
                val today = LocalDate.now()
                (6 downTo 0).map { offset ->
                    val date = today.minusDays(offset.toLong())
                    val moodType = entriesByDate[date]?.moodType
                    RecentMoodUiModel(
                        dayLabel = date.format(DateTimeFormatter.ofPattern("M/d", Locale.KOREA)),
                        emoji = moodType?.emoji,
                        moodType = moodType,
                    )
                }
            }

        _uiState.update {
            it.copy(
                selectedMood = todayMood,
                recentMoods = recentEntries,
                isLoading = false,
            )
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedMood: MoodType? = null,
    val recentMoods: List<RecentMoodUiModel> = emptyList(),
)

data class RecentMoodUiModel(
    val dayLabel: String,
    val emoji: String?,
    val moodType: MoodType?,
)

private val MoodType.emoji: String
    get() = when (this) {
        MoodType.VERY_GOOD -> "\uD83D\uDE01"
        MoodType.GOOD -> "\uD83D\uDE42"
        MoodType.SOSO -> "\uD83D\uDE10"
        MoodType.BAD -> "\uD83D\uDE15"
        MoodType.VERY_BAD -> "\uD83D\uDE22"
    }
