package com.wings.picexchange.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.ui.editor.CardEditorScreen
import com.wings.picexchange.ui.editor.CategoryEditorScreen
import com.wings.picexchange.ui.home.HomeScreen
import com.wings.picexchange.ui.library.LibraryScreen

/** Top-level navigation graph. [onCardClick] adds a tapped library card to the shared strip. */
@Composable
fun PicExchangeNavHost(
    onCardClick: (CardEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { id -> navController.navigate(Routes.library(id)) },
                onAddCategory = { navController.navigate(Routes.categoryEditor()) },
                onEditCategory = { id -> navController.navigate(Routes.categoryEditor(id)) },
            )
        }
        composable(
            route = Routes.LIBRARY,
            arguments = listOf(navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.LongType }),
        ) { entry ->
            val categoryId = entry.arguments?.getLong(Routes.ARG_CATEGORY_ID) ?: 0L
            LibraryScreen(
                onBack = { navController.popBackStack() },
                onCardClick = onCardClick,
                onAddCard = { navController.navigate(Routes.cardEditor(categoryId)) },
                onEditCard = { cardId -> navController.navigate(Routes.cardEditor(categoryId, cardId)) },
            )
        }
        composable(
            route = Routes.CARD_EDITOR,
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_CARD_ID) { type = NavType.LongType; defaultValue = -1L },
            ),
        ) {
            CardEditorScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.CATEGORY_EDITOR,
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) { type = NavType.LongType; defaultValue = -1L },
            ),
        ) {
            CategoryEditorScreen(
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
