package com.sanghyuk.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanghyuk.domain.mood.model.MoodEntry
import com.sanghyuk.domain.mood.model.MoodType
import com.sanghyuk.domain.mood.usecase.GetMoodEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getMoodEntriesUseCase: GetMoodEntriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val today: LocalDate = LocalDate.now()
    private var selectedPeriod: StatisticsPeriod = StatisticsPeriod.WEEKLY
    private var selectedAnchorDate: LocalDate = startOfWeek(today)

    init {
        refresh()
    }

    fun selectPeriod(period: StatisticsPeriod) {
        selectedPeriod = period
        selectedAnchorDate = when (period) {
            StatisticsPeriod.WEEKLY -> startOfWeek(today)
            StatisticsPeriod.MONTHLY -> startOfMonth(today)
        }
        _uiState.update { it.copy(isLoading = true) }
        refresh()
    }

    fun goToPreviousPeriod() {
        selectedAnchorDate = when (selectedPeriod) {
            StatisticsPeriod.WEEKLY -> selectedAnchorDate.minusWeeks(1)
            StatisticsPeriod.MONTHLY -> selectedAnchorDate.minusMonths(1)
        }
        _uiState.update { it.copy(isLoading = true) }
        refresh()
    }

    fun goToNextPeriod() {
        if (!canGoToNextPeriod()) return

        selectedAnchorDate = when (selectedPeriod) {
            StatisticsPeriod.WEEKLY -> selectedAnchorDate.plusWeeks(1)
            StatisticsPeriod.MONTHLY -> selectedAnchorDate.plusMonths(1)
        }
        _uiState.update { it.copy(isLoading = true) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val entries = getMoodEntriesUseCase()
            _uiState.update {
                buildUiState(entries = entries)
            }
        }
    }

    private fun buildUiState(entries: List<MoodEntry>): StatisticsUiState {
        val filteredEntries = when (selectedPeriod) {
            StatisticsPeriod.WEEKLY -> {
                val start = selectedAnchorDate
                val end = start.plusDays(6)
                entries.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            }
            StatisticsPeriod.MONTHLY -> {
                entries.filter {
                    it.date.year == selectedAnchorDate.year &&
                        it.date.month == selectedAnchorDate.month
                }
            }
        }

        val counts = MoodType.entries.associateWith { mood ->
            filteredEntries.count { it.moodType == mood }
        }
        val topMood = counts.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key
        val totalCount = filteredEntries.size
        val maxCount = counts.maxOfOrNull { it.value } ?: 0
        val totalScore = filteredEntries.sumOf { it.moodType.score }
        val encouragementTone = encouragementTone(totalCount = totalCount, totalScore = totalScore)
        val encouragementVariant = randomVariant(encouragementTone)

        return StatisticsUiState(
            isLoading = false,
            selectedPeriod = selectedPeriod,
            periodLabel = formatPeriodLabel(selectedPeriod, selectedAnchorDate),
            totalCount = totalCount,
            topMood = topMood,
            totalScore = totalScore,
            encouragementTone = encouragementTone,
            encouragementVariant = encouragementVariant,
            moodStats = MoodType.entries.map { mood ->
                MoodStatUiModel(
                    moodType = mood,
                    count = counts.getValue(mood),
                    ratio = if (maxCount == 0) 0f else counts.getValue(mood).toFloat() / maxCount.toFloat(),
                )
            },
            canGoNext = canGoToNextPeriod(),
        )
    }

    private fun formatPeriodLabel(period: StatisticsPeriod, anchor: LocalDate): String = when (period) {
        StatisticsPeriod.WEEKLY -> {
            val end = anchor.plusDays(6)
            anchor.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA)) +
                " - " +
                end.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREA))
        }
        StatisticsPeriod.MONTHLY -> {
            anchor.format(DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA))
        }
    }

    private fun encouragementTone(totalCount: Int, totalScore: Int): EncouragementTone {
        if (totalCount == 0) return EncouragementTone.EMPTY

        return when {
            totalScore >= totalCount -> EncouragementTone.VERY_POSITIVE
            totalScore > 0 -> EncouragementTone.POSITIVE
            totalScore <= -totalCount -> EncouragementTone.VERY_NEGATIVE
            totalScore < 0 -> EncouragementTone.NEGATIVE
            else -> EncouragementTone.NEUTRAL
        }
    }

    private fun randomVariant(tone: EncouragementTone): Int {
        val variantCount = when (tone) {
            EncouragementTone.VERY_POSITIVE -> 3
            EncouragementTone.POSITIVE -> 3
            EncouragementTone.NEUTRAL -> 3
            EncouragementTone.NEGATIVE -> 3
            EncouragementTone.VERY_NEGATIVE -> 3
            EncouragementTone.EMPTY -> 2
        }
        return Random.nextInt(variantCount)
    }

    private fun canGoToNextPeriod(): Boolean = when (selectedPeriod) {
        StatisticsPeriod.WEEKLY -> selectedAnchorDate.isBefore(startOfWeek(today))
        StatisticsPeriod.MONTHLY -> selectedAnchorDate.isBefore(startOfMonth(today))
    }

    private fun startOfWeek(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    private fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)
}

enum class StatisticsPeriod {
    WEEKLY,
    MONTHLY,
}

enum class EncouragementTone {
    VERY_POSITIVE,
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    VERY_NEGATIVE,
    EMPTY,
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.WEEKLY,
    val periodLabel: String = "",
    val totalCount: Int = 0,
    val topMood: MoodType? = null,
    val totalScore: Int = 0,
    val encouragementTone: EncouragementTone = EncouragementTone.EMPTY,
    val encouragementVariant: Int = 0,
    val moodStats: List<MoodStatUiModel> = emptyList(),
    val canGoNext: Boolean = false,
)

data class MoodStatUiModel(
    val moodType: MoodType,
    val count: Int,
    val ratio: Float,
)

private val MoodType.score: Int
    get() = when (this) {
        MoodType.VERY_GOOD -> 2
        MoodType.GOOD -> 1
        MoodType.SOSO -> 0
        MoodType.BAD -> -1
        MoodType.VERY_BAD -> -2
    }