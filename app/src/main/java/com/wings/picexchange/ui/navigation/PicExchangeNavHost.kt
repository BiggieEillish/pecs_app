package com.wings.picexchange.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.ui.home.HomeScreen
import com.wings.picexchange.ui.library.LibraryScreen

/** Top-level navigation graph: Home (category grid) -> Library (cards for a category). */
@Composable
fun PicExchangeNavHost(
    onCardClick: (CardEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.library(categoryId))
                },
            )
        }
        composable(
            route = Routes.LIBRARY,
            arguments = listOf(navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.LongType }),
        ) {
            LibraryScreen(
                onBack = { navController.popBackStack() },
                onCardClick = onCardClick,
            )
        }
    }
}
