package com.wings.picexchange.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The app ships 100% offline (no INTERNET permission), so downloadable Google Fonts aren't an
// option. Instead we tune the platform sans into a warmer, larger, bolder scale: comfortable for
// young readers and giving tiles, buttons and titles a friendly, confident weight.
private val Base = Typography()

val Typography = Base.copy(
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.Bold),
    bodyLarge = Base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelSmall = Base.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
)
