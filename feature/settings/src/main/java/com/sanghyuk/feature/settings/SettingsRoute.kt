package com.sanghyuk.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    appVersion: String,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissResetDialog,
            title = {
                Text(text = stringResource(R.string.settings_reset_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.settings_reset_dialog_message))
            },
            confirmButton = {
                TextButton(onClick = viewModel::resetAllRecords) {
                    Text(text = stringResource(R.string.settings_reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissResetDialog) {
                    Text(text = stringResource(R.string.settings_reset_cancel))
                }
            },
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.notificationHour,
            initialMinute = uiState.notificationMinute,
            is24Hour = false,
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(text = stringResource(R.string.settings_notification_time))
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateNotificationTime(
                            timePickerState.hour,
                            timePickerState.minute,
                        )
                        showTimePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.settings_reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = stringResource(R.string.settings_reset_cancel))
                }
            },
        )
    }

    SettingsScreen(
        uiState = uiState,
        appVersion = appVersion,
        onNotificationEnabledChange = viewModel::updateNotificationEnabled,
        onChangeTimeClick = { showTimePicker = true },
        onResetClick = viewModel::showResetDialog,
        onContactClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:hwhwhw944@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.settings_contact_subject))
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(R.string.settings_contact_body) + "\n\nApp Version: $appVersion",
                )
            }
            context.startActivity(intent)
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    appVersion: String,
    onNotificationEnabledChange: (Boolean) -> Unit,
    onChangeTimeClick: () -> Unit,
    onResetClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.settings_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionTitle(title = stringResource(R.string.settings_section_app))
            SettingsInfoCard(
                title = stringResource(R.string.settings_version_title),
                value = appVersion,
            )
        }

        item {
            SectionTitle(title = stringResource(R.string.settings_section_notification))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.settings_notification_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.settings_notification_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = uiState.notificationEnabled,
                            onCheckedChange = onNotificationEnabledChange,
                        )
                    }

                    SettingsActionRow(
                        title = stringResource(R.string.settings_notification_time),
                        description = formatTime(uiState.notificationHour, uiState.notificationMinute),
                        actionLabel = stringResource(R.string.settings_notification_change_time),
                        onClick = onChangeTimeClick,
                    )
                }
            }
        }

        item {
            SectionTitle(title = stringResource(R.string.settings_section_support))
            SettingsActionCard(
                title = stringResource(R.string.settings_contact_title),
                description = stringResource(R.string.settings_contact_description),
                onClick = onContactClick,
            )
        }

        item {
            SectionTitle(title = stringResource(R.string.settings_section_data))
            SettingsActionCard(
                title = stringResource(R.string.settings_reset_title),
                description = stringResource(R.string.settings_reset_description),
                onClick = onResetClick,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SettingsInfoCard(
    title: String,
    value: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String = String.format(
    Locale.KOREA,
    "%02d:%02d",
    hour,
    minute,
)