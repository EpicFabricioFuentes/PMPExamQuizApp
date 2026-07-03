package com.fax.passyourpmpexam.core.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import org.koin.compose.koinInject

/**
 * Anchored adaptive banner sized to the current screen width. The single banner in the app; shown
 * on Home only. Caller supplies width via [modifier] (e.g. `Modifier.fillMaxWidth()`).
 *
 * Renders nothing until UMP consent permits ad requests (see [ConsentManager]).
 */
@Composable
fun HomeBannerAd(modifier: Modifier = Modifier) {
    val consentManager: ConsentManager = koinInject()
    val canRequestAds by consentManager.canRequestAds.collectAsState()
    if (!canRequestAds) return

    val density = LocalDensity.current
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val adWidthDp = with(density) { containerWidthPx.toDp() }.value.toInt()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp),
                )
                adUnitId = AdIds.BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
