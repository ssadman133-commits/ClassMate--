package com.example.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Custom Red-Themed Google AdMob Native Ad Container.
 * Placed directly below Class Routine & Focus Timer.
 *
 * Fully custom red styling:
 * - Red gradient background (#E11D48 -> #DC2626 -> #BE123C)
 * - White circular badge on the left for ad icon
 * - White high-contrast text for Headline, Body, and Advertiser
 * - Crisp white pill Call-To-Action button with bold red text
 * - Seamless AdMob NativeAdView integration with impression and click tracking
 */
@Composable
fun DashboardHubAdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConstants.NATIVE_AD_UNIT_ID,
    bannerHeight: Dp = 96.dp
) {
    val context = LocalContext.current
    val isEmulator = remember { isRunningOnEmulator() }

    val effectiveAdUnitId = remember(adUnitId) {
        if (isEmulator || BuildConfig.DEBUG) {
            AdMobConstants.TEST_NATIVE_AD_UNIT_ID
        } else {
            adUnitId
        }
    }

    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailedToLoad by remember { mutableStateOf(false) }

    // Load native ad on real devices (in background)
    DisposableEffect(effectiveAdUnitId) {
        if (!isEmulator) {
            try {
                val adLoader = AdLoader.Builder(context, effectiveAdUnitId)
                    .forNativeAd { ad ->
                        loadedNativeAd = ad
                        adFailedToLoad = false
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            adFailedToLoad = true
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            .build()
                    )
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())
            } catch (_: Throwable) {
                adFailedToLoad = true
            }
        }

        onDispose {
            loadedNativeAd?.destroy()
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE11D48)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0x48BE123C),
                ambientColor = Color(0x240F172A)
            )
            .clip(RoundedCornerShape(20.dp))
            .testTag("dashboard_hub_ad_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE11D48),
                            Color(0xFFDC2626),
                            Color(0xFFBE123C)
                        )
                    )
                )
        ) {
            val currentAd = loadedNativeAd
            if (currentAd != null && !isEmulator) {
                // Real Live Native Ad View (programmatically bound and styled in red)
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        createRedNativeAdView(ctx, currentAd)
                    },
                    update = { view ->
                        bindNativeAdToView(view, currentAd)
                    }
                )
            } else {
                // Red Native Ad UI (shown on preview, emulator, or while loading)
                RedNativeAdContent(
                    headline = "Special Partner Offer",
                    body = "Explore top academic resources & student discounts",
                    advertiser = "Google AdMob Native",
                    callToAction = "Explore"
                )
            }
        }
    }
}

/**
 * Pure Compose UI for the Red Native Ad
 */
@Composable
private fun RedNativeAdContent(
    headline: String,
    body: String,
    advertiser: String,
    callToAction: String
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: White circle avatar for ad logo/icon
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Ad Icon",
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Middle: Headlines and descriptions
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = headline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Translucent "Ad" badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White.copy(alpha = 0.28f)
                ) {
                    Text(
                        text = "Ad",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Text(
                text = body,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = advertiser,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right: White Pill Call-To-Action Button with bold red text
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = callToAction,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFE11D48)
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Creates and styles a native Android NativeAdView in red theme.
 */
private fun createRedNativeAdView(context: Context, ad: NativeAd): NativeAdView {
    val nativeAdView = NativeAdView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val padH = (14 * context.resources.displayMetrics.density).toInt()
        val padV = (10 * context.resources.displayMetrics.density).toInt()
        setPadding(padH, padV, padH, padV)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    // Left Icon (white circle)
    val iconSize = (50 * context.resources.displayMetrics.density).toInt()
    val iconView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
            marginEnd = (12 * context.resources.displayMetrics.density).toInt()
        }
        val circleBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.WHITE)
        }
        background = circleBg
        clipToOutline = true
        scaleType = ImageView.ScaleType.CENTER_CROP
        val pad = (4 * context.resources.displayMetrics.density).toInt()
        setPadding(pad, pad, pad, pad)
    }

    // Middle Column
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val headlineView = TextView(context).apply {
        setTextColor(android.graphics.Color.WHITE)
        textSize = 15f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = (6 * context.resources.displayMetrics.density).toInt()
        }
    }

    val adBadge = TextView(context).apply {
        text = "Ad"
        setTextColor(android.graphics.Color.WHITE)
        textSize = 9f
        setTypeface(null, Typeface.BOLD)
        val padH2 = (5 * context.resources.displayMetrics.density).toInt()
        val padV2 = (2 * context.resources.displayMetrics.density).toInt()
        setPadding(padH2, padV2, padH2, padV2)
        val badgeBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(android.graphics.Color.argb(70, 255, 255, 255))
        }
        background = badgeBg
    }

    headlineRow.addView(headlineView)
    headlineRow.addView(adBadge)

    val bodyView = TextView(context).apply {
        setTextColor(android.graphics.Color.argb(235, 255, 255, 255))
        textSize = 12f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    val advertiserView = TextView(context).apply {
        setTextColor(android.graphics.Color.argb(200, 255, 255, 255))
        textSize = 11f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    textCol.addView(headlineRow)
    textCol.addView(bodyView)
    textCol.addView(advertiserView)

    // Right Call-to-action button
    val ctaView = TextView(context).apply {
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(android.graphics.Color.parseColor("#E11D48")) // Red text on white button
        gravity = Gravity.CENTER
        val padH3 = (14 * context.resources.displayMetrics.density).toInt()
        val padV3 = (7 * context.resources.displayMetrics.density).toInt()
        setPadding(padH3, padV3, padH3, padV3)
        val btnBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = (16 * context.resources.displayMetrics.density)
            setColor(android.graphics.Color.WHITE)
        }
        background = btnBg
        elevation = 4f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = (8 * context.resources.displayMetrics.density).toInt()
        }
    }

    rootLayout.addView(iconView)
    rootLayout.addView(textCol)
    rootLayout.addView(ctaView)

    nativeAdView.addView(rootLayout)

    // Register views with NativeAdView for proper attribution and interaction
    nativeAdView.headlineView = headlineView
    nativeAdView.bodyView = bodyView
    nativeAdView.iconView = iconView
    nativeAdView.advertiserView = advertiserView
    nativeAdView.callToActionView = ctaView

    bindNativeAdToView(nativeAdView, ad)
    return nativeAdView
}

/**
 * Binds the Google NativeAd payload to the views
 */
private fun bindNativeAdToView(nativeAdView: NativeAdView, ad: NativeAd) {
    (nativeAdView.headlineView as? TextView)?.text = ad.headline

    val iconDrawable = ad.icon?.drawable
    if (iconDrawable != null) {
        (nativeAdView.iconView as? ImageView)?.setImageDrawable(iconDrawable)
        nativeAdView.iconView?.visibility = View.VISIBLE
    } else {
        nativeAdView.iconView?.visibility = View.GONE
    }

    val body = ad.body
    if (!body.isNullOrEmpty()) {
        (nativeAdView.bodyView as? TextView)?.text = body
        nativeAdView.bodyView?.visibility = View.VISIBLE
    } else {
        nativeAdView.bodyView?.visibility = View.GONE
    }

    val advertiser = ad.advertiser
    if (!advertiser.isNullOrEmpty()) {
        (nativeAdView.advertiserView as? TextView)?.text = advertiser
        nativeAdView.advertiserView?.visibility = View.VISIBLE
    } else {
        nativeAdView.advertiserView?.visibility = View.GONE
    }

    val cta = ad.callToAction
    if (!cta.isNullOrEmpty()) {
        (nativeAdView.callToActionView as? TextView)?.text = cta
        nativeAdView.callToActionView?.visibility = View.VISIBLE
    } else {
        (nativeAdView.callToActionView as? TextView)?.text = "Open"
    }

    nativeAdView.setNativeAd(ad)
}
