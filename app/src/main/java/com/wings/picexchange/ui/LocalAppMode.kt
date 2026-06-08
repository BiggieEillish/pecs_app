package com.wings.picexchange.ui

import androidx.compose.runtime.compositionLocalOf
import com.wings.picexchange.data.settings.AppMode

/** App-wide current [AppMode]. Defaults to LEARNER so editing is never exposed before state loads. */
val LocalAppMode = compositionLocalOf { AppMode.LEARNER }
