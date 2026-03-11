package com.sanghyuk.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@Composable
fun SettingsRoute(
    appVersion: String,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.updateNotificationEnabled(true)
        }
    }

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
        WheelTimePickerDialog(
            initialHour = uiState.notificationHour,
            initialMinute = uiState.notificationMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.updateNotificationTime(hour, minute)
                showTimePicker = false
            },
        )
    }

    SettingsScreen(
        uiState = uiState,
        appVersion = appVersion,
        onNotificationEnabledChange = { enabled ->
            if (!enabled) {
                viewModel.updateNotificationEnabled(false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.updateNotificationEnabled(true)
            }
        },
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
                        enabled = uiState.notificationEnabled,
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
private fun WheelTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var selectedHour by remember { mutableIntStateOf(toHour12(initialHour)) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    var selectedPeriod by remember { mutableIntStateOf(if (initialHour < 12) 0 else 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.settings_notification_time))
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpinnerPicker(
                    value = selectedPeriod,
                    range = 0..1,
                    displayedValues = arrayOf("AM", "PM"),
                    onValueChange = { selectedPeriod = it },
                    modifier = Modifier.width(88.dp),
                )
                SpinnerPicker(
                    value = selectedHour,
                    range = 1..12,
                    formatter = { it.toString() },
                    onValueChange = { selectedHour = it },
                    modifier = Modifier.width(88.dp),
                )
                SpinnerPicker(
                    value = selectedMinute,
                    range = 0..59,
                    formatter = { String.format(Locale.KOREA, "%02d", it) },
                    onValueChange = { selectedMinute = it },
                    modifier = Modifier.width(88.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(toHour24(selectedHour, selectedPeriod), selectedMinute)
                },
            ) {
                Text(text = stringResource(R.string.settings_reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_reset_cancel))
            }
        },
    )
}

@Composable
private fun SpinnerPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    displayedValues: Array<String>? = null,
    formatter: ((Int) -> String)? = null,
) {
    val values = displayedValues ?: range.map { formatter?.invoke(it) ?: it.toString() }.toTypedArray()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                wrapSelectorWheel = true
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
            }
        },
        update = { picker ->
            picker.minValue = range.first
            picker.maxValue = range.last
            picker.displayedValues = null
            picker.displayedValues = values
            if (picker.value != value) {
                picker.value = value
            }
        },
    )
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
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
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
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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

private fun toHour12(hour24: Int): Int = when (val normalized = hour24 % 12) {
    0 -> 12
    else -> normalized
}

private fun toHour24(hour12: Int, period: Int): Int = when {
    period == 0 && hour12 == 12 -> 0
    period == 0 -> hour12
    period == 1 && hour12 == 12 -> 12
    else -> hour12 + 12
}