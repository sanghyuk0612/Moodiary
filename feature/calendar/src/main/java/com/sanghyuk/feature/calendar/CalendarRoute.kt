package com.sanghyuk.feature.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.domain.mood.model.MoodType
import java.time.LocalDate

@Composable
fun CalendarRoute(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::goToPreviousMonth,
        onNextMonth = viewModel::goToNextMonth,
        modifier = modifier,
    )
}

@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.calendar_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(onClick = onPreviousMonth) {
                        Text(text = stringResource(R.string.calendar_previous_month))
                    }
                    Text(
                        text = uiState.monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    FilledTonalButton(onClick = onNextMonth) {
                        Text(text = stringResource(R.string.calendar_next_month))
                    }
                }
                Text(
                    text = stringResource(R.string.calendar_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SummaryCard(uiState = uiState)
        }

        item {
            CalendarGrid(uiState = uiState)
        }
    }
}

@Composable
private fun SummaryCard(uiState: CalendarUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.calendar_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (uiState.recordedDays == 0) {
                Text(
                    text = stringResource(R.string.calendar_summary_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = stringResource(R.string.calendar_summary_total, uiState.recordedDays),
                    style = MaterialTheme.typography.bodyLarge,
                )
                uiState.topMood?.let { mood ->
                    Text(
                        text = stringResource(
                            R.string.calendar_summary_top,
                            stringResource(mood.labelResId),
                            mood.emoji,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(uiState: CalendarUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekdayHeaderResIds().forEach { resId ->
                Text(
                    text = stringResource(resId),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        uiState.weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDayUiModel,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        day.moodType == null -> MaterialTheme.colorScheme.surface
        else -> day.moodType.backgroundColor()
    }
    val contentColor = if (day.moodType == MoodType.VERY_BAD) Color(0xFF5D2C2C) else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(width = if (day.isToday) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .background(color = containerColor, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayOfMonthLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (day.isCurrentMonth) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = day.emoji ?: "",
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

private fun weekdayHeaderResIds(): List<Int> = listOf(
    R.string.calendar_day_monday,
    R.string.calendar_day_tuesday,
    R.string.calendar_day_wednesday,
    R.string.calendar_day_thursday,
    R.string.calendar_day_friday,
    R.string.calendar_day_saturday,
    R.string.calendar_day_sunday,
)

private fun MoodType.backgroundColor(): Color = when (this) {
    MoodType.VERY_GOOD -> Color(0xFFDDF6D8)
    MoodType.GOOD -> Color(0xFFE6F4EA)
    MoodType.SOSO -> Color(0xFFF4EFD8)
    MoodType.BAD -> Color(0xFFF9E0D2)
    MoodType.VERY_BAD -> Color(0xFFF6D6D9)
}

private val MoodType.emoji: String
    get() = when (this) {
        MoodType.VERY_GOOD -> "\uD83D\uDE01"
        MoodType.GOOD -> "\uD83D\uDE42"
        MoodType.SOSO -> "\uD83D\uDE10"
        MoodType.BAD -> "\uD83D\uDE15"
        MoodType.VERY_BAD -> "\uD83D\uDE22"
    }

@get:StringRes
private val MoodType.labelResId: Int
    get() = when (this) {
        MoodType.VERY_GOOD -> R.string.mood_very_good_label
        MoodType.GOOD -> R.string.mood_good_label
        MoodType.SOSO -> R.string.mood_soso_label
        MoodType.BAD -> R.string.mood_bad_label
        MoodType.VERY_BAD -> R.string.mood_very_bad_label
    }

@Preview(showBackground = true)
@Composable
private fun CalendarRoutePreview() {
    MoodiaryTheme {
        CalendarScreen(
            uiState = CalendarUiState(
                isLoading = false,
                monthTitle = "2026년 3월",
                recordedDays = 8,
                topMood = MoodType.GOOD,
                weeks = previewCalendarWeeks(),
            ),
            onPreviousMonth = {},
            onNextMonth = {},
        )
    }
}

private fun previewCalendarWeeks(): List<List<CalendarDayUiModel>> {
    val baseDate = LocalDate.of(2026, 2, 23)
    return List(5) { weekIndex ->
        List(7) { dayIndex ->
            val offset = weekIndex * 7 + dayIndex
            val date = baseDate.plusDays(offset.toLong())
            val mood = when {
                date.monthValue != 3 -> null
                offset % 5 == 0 -> MoodType.VERY_GOOD
                offset % 4 == 0 -> MoodType.BAD
                offset % 3 == 0 -> MoodType.GOOD
                else -> null
            }
            CalendarDayUiModel(
                date = date,
                dayOfMonthLabel = date.dayOfMonth.toString(),
                isCurrentMonth = date.monthValue == 3,
                isToday = date == LocalDate.of(2026, 3, 11),
                moodType = mood,
                emoji = mood?.emoji,
            )
        }
    }
}
