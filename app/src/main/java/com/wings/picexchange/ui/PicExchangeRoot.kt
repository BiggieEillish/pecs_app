package com.wings.picexchange.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wings.picexchange.ui.navigation.PicExchangeNavHost
import com.wings.picexchange.ui.strip.SentenceStrip
import com.wings.picexchange.ui.strip.StripViewModel
import kotlinx.coroutines.launch

/**
 * App root. Owns the single Activity-scoped [StripViewModel] (obtained here, above the NavHost, so
 * the sentence strip persists across navigation) and hosts the persistent strip + Undo snackbar.
 */
@Composable
fun PicExchangeRoot() {
    val stripViewModel: StripViewModel = hiltViewModel()
    val items by stripViewModel.items.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SentenceStrip(
                items = items,
                onRemove = stripViewModel::remove,
                onClear = {
                    val previous = stripViewModel.clear()
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Sentence cleared",
                            actionLabel = "Undo",
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            stripViewModel.restore(previous)
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        PicExchangeNavHost(
            onCardClick = stripViewModel::add,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
