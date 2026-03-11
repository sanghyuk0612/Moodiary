package com.sanghyuk.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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

    val moodRows = listOf(
        MoodType.entries.take(3),
        MoodType.entries.drop(3),
    )

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
                moodRows.forEachIndexed { index, rowMoods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (index == 1) {
                            Spacer(modifier = Modifier.weight(0.5f))
                        }

                        rowMoods.forEach { mood ->
                            MoodChip(
                                emoji = mood.emoji,
                                label = stringResource(mood.labelResId),
                                selected = uiState.selectedMood == mood,
                                backgroundColor = mood.backgroundColor(),
                                onClick = { onMoodSelected(mood) },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        repeat(3 - rowMoods.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        if (index == 1) {
                            Spacer(modifier = Modifier.weight(0.5f))
                        }
                    }
                }
            }
        }
        Text(
            text = uiState.selectedMood?.let {
                stringResource(
                    R.string.home_selected_mood,
                    stringResource(it.labelResId),
                    it.emoji,
                )
            } ?: stringResource(R.string.home_empty_state),
            style = MaterialTheme.typography.titleMedium,
        )
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
            uiState = HomeUiState(isLoading = false, selectedMood = MoodType.GOOD),
            onMoodSelected = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}
