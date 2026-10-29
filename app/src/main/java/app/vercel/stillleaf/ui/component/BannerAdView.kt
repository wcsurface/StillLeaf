package app.vercel.stillleaf.ui.component

import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.vercel.stillleaf.data.model.SubscriptionStatus
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    subscriptionStatus: SubscriptionStatus,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // AdMob Sample Banner Unit ID
) {
    // Suppress ad rendering completely for PRO subscribers
    if (subscriptionStatus.isPro) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 4.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdUnitId(adUnitId)

                    val displayMetrics: DisplayMetrics = ctx.resources.displayMetrics
                    val adWidthPixels = displayMetrics.widthPixels
                    val density = displayMetrics.density
                    val adWidth = (adWidthPixels / density).toInt()

                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
