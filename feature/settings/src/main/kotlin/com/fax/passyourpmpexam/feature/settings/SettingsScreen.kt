package com.fax.passyourpmpexam.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme
import com.fax.passyourpmpexam.core.domain.model.DailyGoal
import com.fax.passyourpmpexam.core.domain.model.ThemeMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
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
        onOpenAbout = onOpenAbout,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onEnableReminder: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Pinned bar; the large title below it scrolls away (One UI style, matching Home).
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PmpSpacing.safeMargin)
                .padding(bottom = PmpSpacing.basePadding),
            verticalArrangement = Arrangement.spacedBy(PmpSpacing.basePadding),
        ) {
            Text("Settings", style = MaterialTheme.typography.displayLarge)

            SettingsSection("Appearance") {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    if (index > 0) BentoDivider()
                    ThemeOptionRow(
                        label = mode.label(),
                        selected = state.themeMode == mode,
                        onSelect = { onIntent(SettingsIntent.SetTheme(mode)) },
                    )
                }
            }

            SettingsSection("Study goal") {
                DailyGoalRow(
                    goal = state.dailyGoal,
                    onGoalChange = { onIntent(SettingsIntent.SetDailyGoal(it)) },
                )
            }

            SettingsSection("Daily reminder") {
                ReminderToggleRow(
                    enabled = state.reminderEnabled,
                    onEnabledChange = { enabled ->
                        if (enabled) onEnableReminder()
                        else onIntent(SettingsIntent.SetReminderEnabled(false))
                    },
                )
                if (state.reminderEnabled) {
                    BentoDivider()
                    ReminderTimeRow(
                        minuteOfDay = state.reminderMinuteOfDay,
                        onTimeSelected = { onIntent(SettingsIntent.SetReminderTime(it)) },
                    )
                }
            }

            SettingsSection("About") {
                RowItem(modifier = Modifier.clickable(onClick = onOpenAbout)) {
                    Text("About the app", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin)
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = PmpSpacing.basePadding, vertical = PmpSpacing.safeMargin),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun DailyGoalRow(goal: Int, onGoalChange: (Int) -> Unit) {
    RowItem {
        Column(modifier = Modifier.weight(1f)) {
            Text("Questions per day", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Counts across all modes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(PmpSpacing.gridUnit),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PmpSpacing.gridUnit),
        ) {
            StepButton(glyph = "−", enabled = goal > DailyGoal.MIN) { onGoalChange(goal - 1) }
            Text(
                text = goal.toString(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 40.dp),
            )
            StepButton(glyph = "+", enabled = goal < DailyGoal.MAX) { onGoalChange(goal + 1) }
        }
    }
}

/** A round 40dp increment/decrement button with a text glyph (no icon-font dependency). */
@Composable
private fun StepButton(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.headlineMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
        )
    }
}

@Composable
private fun ReminderToggleRow(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    RowItem {
        Text("Remind me to study", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}

@Composable
private fun ReminderTimeRow(minuteOfDay: Int, onTimeSelected: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    RowItem(modifier = Modifier.clickable { showDialog = true }) {
        Text("Reminder time", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = formatMinuteOfDay(minuteOfDay),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

@Preview
@Composable
private fun SettingsContentPreview() {
    PmpTheme {
        SettingsContent(
            state = SettingsUiState(
                themeMode = ThemeMode.SYSTEM,
                reminderEnabled = true,
                reminderMinuteOfDay = 20 * 60,
                dailyGoal = 5,
            ),
            onIntent = {},
            onEnableReminder = {},
            onOpenAbout = {},
        )
    }
}
