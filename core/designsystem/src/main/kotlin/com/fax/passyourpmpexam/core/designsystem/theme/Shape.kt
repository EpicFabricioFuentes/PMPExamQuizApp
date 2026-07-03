package com.fax.passyourpmpexam.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale from the SSOT: buttons/chips use 16px (rounded-lg), cards/bottom-sheets
 * use 24px (rounded-xl). No sharp 90° angles anywhere.
 */
val PmpShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),      // buttons, selection chips
    extraLarge = RoundedCornerShape(24.dp), // content cards, bottom sheets
)
