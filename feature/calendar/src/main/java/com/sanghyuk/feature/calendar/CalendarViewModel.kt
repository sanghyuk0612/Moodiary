package com.sanghyuk.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.model.MoodType
import com.sanghyuk.domain.mood.usecase.GetMoodEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMoodEntriesUseCase: GetMoodEntriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1)

    init {
        refresh()
    }

    fun refresh() {
        loadCalendar(selectedMonth)
    }

    fun goToPreviousMonth() {
        selectedMonth = selectedMonth.minusMonths(1)
        loadCalendar(selectedMonth)
    }

    fun goToNextMonth() {
        selectedMonth = selectedMonth.plusMonths(1)
        loadCalendar(selectedMonth)
    }

    private fun loadCalendar(month: LocalDate) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val entries = getMoodEntriesUseCase()
            _uiState.update {
                buildUiState(month = month, entries = entries)
            }
        }
    }

    private fun buildUiState(
        month: LocalDate,
        entries: List<MoodEntry>,
    ): CalendarUiState {
        val entryMap = entries.associateBy { it.date }
        val currentMonthEntries = entries.filter { it.date.year == month.year && it.date.month == month.month }
        val topMood = currentMonthEntries
            .groupingBy { it.moodType }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return CalendarUiState(
            isLoading = false,
            monthTitle = month.format(DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)),
            recordedDays = currentMonthEntries.size,
            topMood = topMood,
            weeks = buildWeeks(month = month, entryMap = entryMap),
        )
    }

    private fun buildWeeks(
        month: LocalDate,
        entryMap: Map<LocalDate, MoodEntry>,
    ): List<List<CalendarDayUiModel>> {
        val firstDayOfMonth = month.withDayOfMonth(1)
        val startOffset = firstDayOfMonth.dayOfWeek.ordinal
        val calendarStart = firstDayOfMonth.minusDays(startOffset.toLong())
        val lastDayOfMonth = month.withDayOfMonth(month.lengthOfMonth())
        val endOffset = DayOfWeek.SUNDAY.ordinal - lastDayOfMonth.dayOfWeek.ordinal
        val calendarEnd = lastDayOfMonth.plusDays(endOffset.toLong())
        val dayCount = ChronoUnit.DAYS.between(calendarStart, calendarEnd).toInt()
        val allDays = (0..dayCount).map { calendarStart.plusDays(it.toLong()) }

        return allDays.chunked(7).map { week ->
            week.map { date ->
                val mood = entryMap[date]?.moodType
                CalendarDayUiModel(
                    date = date,
                    dayOfMonthLabel = date.dayOfMonth.toString(),
                    isCurrentMonth = date.month == month.month,
                    isToday = date == LocalDate.now(),
                    moodType = mood,
                    emoji = mood?.toEmoji(),
                )
            }
        }
    }
}

data class CalendarUiState(
    val isLoading: Boolean = true,
    val monthTitle: String = "",
    val recordedDays: Int = 0,
    val topMood: MoodType? = null,
    val weeks: List<List<CalendarDayUiModel>> = emptyList(),
)

data class CalendarDayUiModel(
    val date: LocalDate,
    val dayOfMonthLabel: String,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val moodType: MoodType?,
    val emoji: String?,
)

private fun MoodType.toEmoji(): String = when (this) {
    MoodType.VERY_GOOD -> "\uD83D\uDE01"
    MoodType.GOOD -> "\uD83D\uDE42"
    MoodType.SOSO -> "\uD83D\uDE10"
    MoodType.BAD -> "\uD83D\uDE15"
    MoodType.VERY_BAD -> "\uD83D\uDE22"
}
