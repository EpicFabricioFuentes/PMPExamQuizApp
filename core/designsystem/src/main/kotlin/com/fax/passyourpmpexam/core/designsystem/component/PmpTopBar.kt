package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * App bar for pushed mode screens (Daily / Quiz / Free). Shows [title] and a back arrow that calls
 * [onBack] — a discoverable alternative to the system back gesture.
 *
 * Window insets are zeroed on purpose: the app's outer [androidx.compose.material3.Scaffold] has no
 * top bar, so its content padding already accounts for the status bar. A default [TopAppBar] would
 * apply that inset a second time and leave a gap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PmpTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
    )
}
