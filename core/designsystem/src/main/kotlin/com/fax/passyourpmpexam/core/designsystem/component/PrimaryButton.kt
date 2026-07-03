package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/** Full-width primary action button, following the SSOT 16px corner radius + 48px touch target. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    PmpTheme {
        PrimaryButton(text = "Check answer", onClick = {})
    }
}
