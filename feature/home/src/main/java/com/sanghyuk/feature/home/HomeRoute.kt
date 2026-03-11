package com.sanghyuk.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sanghyuk.designsystem.component.MoodChip
import com.sanghyuk.designsystem.theme.MoodiaryTheme
import com.sanghyuk.domain.mood.model.MoodType

@Composable
fun HomeRoute() {
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }

    Scaffold { innerPadding ->
        HomeScreen(
            selectedMood = selectedMood,
            onMoodSelected = { selectedMood = it },
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun HomeScreen(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
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
                MoodType.entries.toList().chunked(3).forEach { rowMoods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowMoods.forEach { mood ->
                            MoodChip(
                                emoji = mood.emoji,
                                label = stringResource(mood.labelResId),
                                selected = selectedMood == mood,
                                onClick = { onMoodSelected(mood) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - rowMoods.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Text(
            text = selectedMood?.let {
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
        HomeRoute()
    }
}
