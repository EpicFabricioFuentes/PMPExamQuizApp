package com.fax.passyourpmpexam.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fax.passyourpmpexam.core.designsystem.theme.PmpSpacing
import com.fax.passyourpmpexam.core.designsystem.theme.PmpTheme

/**
 * Full-width primary action button, following the SSOT 16px corner radius + 48px touch target.
 * Pass [trailingIcon] to render an icon after the label (e.g. a forward arrow on "Continue").
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PmpSpacing.touchTargetMin),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, style = MaterialTheme.typography.titleSmall)
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = PmpSpacing.gridUnit)
                        .size(18.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    PmpTheme {
        PrimaryButton(text = "Continue", onClick = {}, trailingIcon = Icons.AutoMirrored.Filled.ArrowForward)
    }
}
