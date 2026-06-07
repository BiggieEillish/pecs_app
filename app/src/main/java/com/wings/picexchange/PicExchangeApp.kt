package com.wings.picexchange

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Annotated for Hilt so the DI graph is generated at build time. */
@HiltAndroidApp
class PicExchangeApp : Application()
