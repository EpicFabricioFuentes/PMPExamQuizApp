package com.fax.passyourpmpexam.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import org.koin.androidx.compose.koinViewModel

// TODO: replace with the live hosted URL once docs/privacy-policy.md is published (see that file's header).
private const val PRIVACY_POLICY_URL = "https://example.com/privacy-policy"

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    // On API 33+ enabling the reminder requires POST_NOTIFICATIONS. Scheduling happens regardless;
    // the notifier silently skips posting if the permission is denied.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result ignored — scheduling already requested */ }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onEnableReminder = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewModel.onIntent(SettingsIntent.SetReminderEnabled(true))
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onEnableReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PmpSpacing.basePadding),
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SectionHeader("Appearance")
        ThemeMode.entries.forEach { mode ->
            ThemeOptionRow(
                label = mode.label(),
                selected = state.themeMode == mode,
                onSelect = { onIntent(SettingsIntent.SetTheme(mode)) },
            )
        }

        SectionHeader("Daily reminder")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Remind me to study", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.reminderEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) onEnableReminder()
                    else onIntent(SettingsIntent.SetReminderEnabled(false))
                },
            )
        }
        if (state.reminderEnabled) {
            ReminderTimeRow(
                minuteOfDay = state.reminderMinuteOfDay,
                onTimeSelected = { onIntent(SettingsIntent.SetReminderTime(it)) },
            )
        }

        SectionHeader("About")
        Text("PMP Prep · v1.0", style = MaterialTheme.typography.bodyMedium)
        PrivacyPolicyRow()
    }
}

@Composable
private fun PrivacyPolicyRow() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin)
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ReminderTimeRow(minuteOfDay: Int, onTimeSelected: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Reminder time", style = MaterialTheme.typography.bodyLarge)
        Text(formatMinuteOfDay(minuteOfDay), style = MaterialTheme.typography.bodyLarge)
    }
    if (showDialog) {
        TimePickerDialog(
            initialMinuteOfDay = minuteOfDay,
            onConfirm = {
                onTimeSelected(it)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialMinuteOfDay: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val timeState = rememberTimePickerState(
        initialHour = initialMinuteOfDay / 60,
        initialMinute = initialMinuteOfDay % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timeState.hour * 60 + timeState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Reminder time", textAlign = TextAlign.Center) },
        text = { TimePicker(state = timeState) },
    )
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun formatMinuteOfDay(minuteOfDay: Int): String {
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return "%02d:%02d".format(hour, minute)
}
