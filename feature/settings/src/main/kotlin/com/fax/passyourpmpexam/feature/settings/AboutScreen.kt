package com.fax.passyourpmpexam.feature.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.fax.passyourpmpexam.core.designsystem.component.PmpTopBar
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

// Placeholders — replace with real values before release (see also docs/privacy-policy.md).
// TODO: replace with the live hosted URL once docs/privacy-policy.md is published.
private const val PRIVACY_POLICY_URL = "https://example.com/privacy-policy"
// TODO: replace with the real support address.
private const val SUPPORT_EMAIL = "support@example.com"
// TODO: replace with the published Play Store listing URL once the app ships.
private const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id="
private const val SHARE_TEXT =
    "Prepping for the PMP exam? Try PMP Prep: $PLAY_STORE_WEB_URL"

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PmpTopBar(title = "About", onBack = onBack) },
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
            SettingsSection("App") {
                RowItem {
                    Text("PMP Prep", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = rememberVersionName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SettingsSection("Support") {
                RateRow()
                BentoDivider()
                ShareRow()
                BentoDivider()
                ContactRow()
                BentoDivider()
                PrivacyPolicyRow()
            }

            Footer()
        }
    }
}

@Composable
private fun RateRow() {
    val context = LocalContext.current
    ActionRow(label = "Rate the app") {
        val marketUri = "market://details?id=${context.packageName}".toUri()
        val webUri = "$PLAY_STORE_WEB_URL${context.packageName}".toUri()
        // Prefer the Play Store app; fall back to the browser if it isn't installed.
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, marketUri)) }
            .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) }
    }
}

@Composable
private fun ShareRow() {
    val context = LocalContext.current
    ActionRow(label = "Share the app") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, SHARE_TEXT)
        }
        context.startActivity(Intent.createChooser(send, null))
    }
}

@Composable
private fun ContactRow() {
    val context = LocalContext.current
    ActionRow(label = "Contact / Feedback") {
        val email = Intent(Intent.ACTION_SENDTO, "mailto:$SUPPORT_EMAIL".toUri()).apply {
            putExtra(Intent.EXTRA_SUBJECT, "PMP Prep feedback")
        }
        context.startActivity(Intent.createChooser(email, null))
    }
}

@Composable
private fun PrivacyPolicyRow() {
    val context = LocalContext.current
    RowItem(
        modifier = Modifier.clickable {
            context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri()))
        },
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** A tappable row: a label plus a trailing "›" glyph (text glyph avoids an icon-font dependency). */
@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    RowItem(modifier = Modifier.clickable(onClick = onClick)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PmpSpacing.basePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmpSpacing.itemGap),
    ) {
        // Trademark fine print: this is an independent study aid, not affiliated with PMI.
        Text(
            text = "PMP® is a registered mark of the Project Management Institute, Inc. " +
                    "This project is an independent study aid and is not affiliated with or endorsed by PMI®.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Made with ❤️ by Fax Development Studios",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    PmpTheme {
        AboutScreen(onBack = {})
    }
}
