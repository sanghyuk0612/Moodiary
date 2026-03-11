package com.sanghyuk.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.sanghyuk.designsystem.component.MoodChip
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.domain.mood.model.MoodType

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    HomeScreen(
        uiState = uiState,
        onMoodSelected = viewModel::onMoodSelected,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onMoodSelected: (MoodType) -> Unit,
    contentPadding: PaddingValues,
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

    val firstRow = MoodType.entries.take(3)
    val secondRow = MoodType.entries.drop(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.home_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MoodRow(
                    moods = firstRow,
                    selectedMood = uiState.selectedMood,
                    onMoodSelected = onMoodSelected,
                )
                MoodRow(
                    moods = secondRow,
                    selectedMood = uiState.selectedMood,
                    onMoodSelected = onMoodSelected,
                    centerAligned = true,
                )
            }
        }
        SelectedMoodCard(selectedMood = uiState.selectedMood)
        RecentMoodCard(recentMoods = uiState.recentMoods)
    }
}

@Composable
private fun MoodRow(
    moods: List<MoodType>,
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    centerAligned: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (centerAligned) {
            Spacer(modifier = Modifier.weight(0.5f))
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
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

@Composable
private fun SelectedMoodCard(selectedMood: MoodType?) {
    val backgroundColor = selectedMood?.backgroundColor() ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text(
                text = selectedMood?.let {
                    stringResource(
                        R.string.home_selected_mood,
                        stringResource(it.labelResId),
                        it.emoji,
                    )
                } ?: stringResource(R.string.home_empty_state),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RecentMoodCard(recentMoods: List<RecentMoodUiModel>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_recent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.home_recent_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recentMoods.forEach { mood ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = mood.moodType?.backgroundColor()
                                    ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = mood.dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = mood.emoji ?: "-",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
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
private fun HomeRoutePreview() {
    MoodiaryTheme {
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                selectedMood = MoodType.GOOD,
                recentMoods = listOf(
                    RecentMoodUiModel("3/5", "\uD83D\uDE42", MoodType.GOOD),
                    RecentMoodUiModel("3/6", "\uD83D\uDE01", MoodType.VERY_GOOD),
                    RecentMoodUiModel("3/7", null, null),
                    RecentMoodUiModel("3/8", "\uD83D\uDE22", MoodType.VERY_BAD),
                    RecentMoodUiModel("3/9", "\uD83D\uDE10", MoodType.SOSO),
                    RecentMoodUiModel("3/10", "\uD83D\uDE42", MoodType.GOOD),
                    RecentMoodUiModel("3/11", "\uD83D\uDE42", MoodType.GOOD),
                ),
            ),
            onMoodSelected = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}