package com.sanghyuk.feature.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanghyuk.designsystem.component.MoodChip
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.domain.mood.model.MoodType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        onDateSelected = viewModel::onDateSelected,
        onDismissEditor = viewModel::dismissEditor,
        onMoodSelected = viewModel::saveMood,
        onDeleteMood = viewModel::deleteMood,
        modifier = modifier,
    )
}

@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDismissEditor: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    onDeleteMood: () -> Unit,
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

    uiState.editingDate?.let { editingDate ->
        MoodPickerDialog(
            date = editingDate,
            selectedMood = uiState.editingMood,
            onDismiss = onDismissEditor,
            onMoodSelected = onMoodSelected,
            onDeleteMood = onDeleteMood,
        )
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
                    FilledTonalButton(onClick = onNextMonth, enabled = uiState.canGoNext) {
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
            CalendarGrid(
                uiState = uiState,
                onDateSelected = onDateSelected,
            )
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
private fun CalendarGrid(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
) {
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
                        onClick = { onDateSelected(day.date) },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
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
            .clip(shape)
            .border(width = if (day.isToday) 1.5.dp else 1.dp, color = borderColor, shape = shape)
            .background(color = containerColor, shape = shape)
            .clickable(enabled = !day.isFuture, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayOfMonthLabel,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                day.isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                day.isCurrentMonth -> contentColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = day.emoji ?: "",
            style = MaterialTheme.typography.titleSmall,
            color = if (day.isFuture) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else Color.Unspecified,
        )
    }
}

@Composable
private fun MoodPickerDialog(
    date: LocalDate,
    selectedMood: MoodType?,
    onDismiss: () -> Unit,
    onMoodSelected: (MoodType) -> Unit,
    onDeleteMood: () -> Unit,
) {
    val firstRow = MoodType.entries.take(3)
    val secondRow = MoodType.entries.drop(3)
    val formattedDate = date.format(DateTimeFormatter.ofPattern("M/d", Locale.KOREA))
    val messageResId = if (selectedMood == null) {
        R.string.calendar_picker_message
    } else {
        R.string.calendar_picker_edit_message
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.calendar_picker_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(messageResId, formattedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoodPickerRow(
                    moods = firstRow,
                    selectedMood = selectedMood,
                    onMoodSelected = onMoodSelected,
                )
                MoodPickerRow(
                    moods = secondRow,
                    selectedMood = selectedMood,
                    onMoodSelected = onMoodSelected,
                    centerAligned = true,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedMood != null) {
                    TextButton(onClick = onDeleteMood) {
                        Text(text = stringResource(R.string.calendar_picker_delete))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.calendar_picker_close))
                }
            }
        },
    )
}

@Composable
private fun MoodPickerRow(
    moods: List<MoodType>,
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    centerAligned: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (centerAligned) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(0.5f))
        }

        moods.forEach { mood ->
            MoodChip(
                emoji = mood.emoji,
                label = stringResource(mood.labelResId),
                selected = selectedMood == mood,
                backgroundColor = mood.backgroundColor(),
                onClick = { onMoodSelected(mood) },
                modifier = Modifier.weight(1f),
            )
        }

        if (centerAligned) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(0.5f))
        }
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
                monthTitle = "2026.03",
                recordedDays = 8,
                topMood = MoodType.GOOD,
                weeks = previewCalendarWeeks(),
                canGoNext = false,
                editingDate = LocalDate.of(2026, 3, 10),
                editingMood = MoodType.GOOD,
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onDateSelected = {},
            onDismissEditor = {},
            onMoodSelected = {},
            onDeleteMood = {},
        )
    }
}

private fun previewCalendarWeeks(): List<List<CalendarDayUiModel>> {
    val baseDate = LocalDate.of(2026, 2, 23)
    val today = LocalDate.of(2026, 3, 11)
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
                isToday = date == today,
                isFuture = date.isAfter(today),
                moodType = mood,
                emoji = mood?.emoji,
            )
        }
    }
}