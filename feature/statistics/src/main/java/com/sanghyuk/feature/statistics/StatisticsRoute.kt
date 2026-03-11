package com.sanghyuk.feature.statistics

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.domain.mood.model.MoodType

@Composable
fun StatisticsRoute(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    StatisticsScreen(
        uiState = uiState,
        onPeriodSelected = viewModel::selectPeriod,
        onPreviousPeriod = viewModel::goToPreviousPeriod,
        onNextPeriod = viewModel::goToNextPeriod,
        modifier = modifier,
    )
}

@Composable
private fun StatisticsScreen(
    uiState: StatisticsUiState,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.statistics_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.statistics_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatisticsPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = {
                            Text(
                                text = stringResource(
                                    if (period == StatisticsPeriod.WEEKLY) {
                                        R.string.statistics_weekly
                                    } else {
                                        R.string.statistics_monthly
                                    },
                                ),
                            )
                        },
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledTonalButton(onClick = onPreviousPeriod) {
                            Text(text = stringResource(R.string.statistics_previous_period))
                        }
                        Text(
                            text = uiState.periodLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FilledTonalButton(
                            onClick = onNextPeriod,
                            enabled = uiState.canGoNext,
                        ) {
                            Text(text = stringResource(R.string.statistics_next_period))
                        }
                    }
                    if (uiState.totalCount == 0) {
                        Text(
                            text = stringResource(R.string.statistics_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.statistics_total_records, uiState.totalCount),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        uiState.topMood?.let { mood ->
                            Text(
                                text = stringResource(
                                    R.string.statistics_top_mood,
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

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.statistics_section_distribution),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    uiState.moodStats.forEach { stat ->
                        MoodStatRow(stat = stat)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.statistics_section_message),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = encouragementMessage(
                            tone = uiState.encouragementTone,
                            variant = uiState.encouragementVariant,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodStatRow(stat: MoodStatUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${stat.moodType.emoji} ${stringResource(stat.moodType.labelResId)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stat.count.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(999.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.ratio.coerceIn(0f, 1f))
                    .height(12.dp)
                    .background(
                        color = stat.moodType.backgroundColor(),
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
        }
    }
}

@Composable
private fun encouragementMessage(
    tone: EncouragementTone,
    variant: Int,
): String {
    val resId = when (tone) {
        EncouragementTone.VERY_POSITIVE -> when (variant) {
            0 -> R.string.statistics_message_very_positive_1
            1 -> R.string.statistics_message_very_positive_2
            else -> R.string.statistics_message_very_positive_3
        }
        EncouragementTone.POSITIVE -> when (variant) {
            0 -> R.string.statistics_message_positive_1
            1 -> R.string.statistics_message_positive_2
            else -> R.string.statistics_message_positive_3
        }
        EncouragementTone.NEUTRAL -> when (variant) {
            0 -> R.string.statistics_message_neutral_1
            1 -> R.string.statistics_message_neutral_2
            else -> R.string.statistics_message_neutral_3
        }
        EncouragementTone.NEGATIVE -> when (variant) {
            0 -> R.string.statistics_message_negative_1
            1 -> R.string.statistics_message_negative_2
            else -> R.string.statistics_message_negative_3
        }
        EncouragementTone.VERY_NEGATIVE -> when (variant) {
            0 -> R.string.statistics_message_very_negative_1
            1 -> R.string.statistics_message_very_negative_2
            else -> R.string.statistics_message_very_negative_3
        }
        EncouragementTone.EMPTY -> if (variant == 0) {
            R.string.statistics_message_empty_1
        } else {
            R.string.statistics_message_empty_2
        }
    }
    return stringResource(resId)
}

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
private fun StatisticsRoutePreview() {
    MoodiaryTheme {
        StatisticsScreen(
            uiState = StatisticsUiState(
                isLoading = false,
                selectedPeriod = StatisticsPeriod.WEEKLY,
                periodLabel = "3월 10일 - 3월 16일",
                totalCount = 4,
                topMood = MoodType.GOOD,
                totalScore = 2,
                encouragementTone = EncouragementTone.POSITIVE,
                encouragementVariant = 1,
                moodStats = listOf(
                    MoodStatUiModel(MoodType.VERY_GOOD, 1, 0.5f),
                    MoodStatUiModel(MoodType.GOOD, 2, 1f),
                    MoodStatUiModel(MoodType.SOSO, 1, 0.5f),
                    MoodStatUiModel(MoodType.BAD, 0, 0f),
                    MoodStatUiModel(MoodType.VERY_BAD, 0, 0f),
                ),
                canGoNext = false,
            ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
        )
    }
}