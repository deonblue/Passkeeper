package com.sevennotes.passkeeper.ui.pages

import android.content.ClipboardManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sevennotes.passkeeper.ui.viewmodel.MainViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startDestination: String,
    searchStr: String,
    cm: ClipboardManager,
) {
    val focusor = LocalFocusManager.current
    val addPageState = mainViewModel.addPageState
    val editCategoryState by mainViewModel.editCategoryState.collectAsState()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screens.MAIN_SCREEN) {
            PassList(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { focusor.clearFocus() }
                    },
                passListState = mainViewModel.passListState,
                searchStr = searchStr,
                cm = cm,
                decode = mainViewModel::decodeString,
                onPasswordClick = {
                    mainViewModel.updateNewPassword(it)
                    navController.navigate(Screens.ADD_SCREEN)
                }
            )
        }

        composable(route = Screens.ADD_SCREEN) {
            AddPage(
                addPageState = addPageState,
                openEditCategory = { navController.navigate(Screens.CATE_SCREEN)}
            )
        }

        composable(route = Screens.CATE_SCREEN) {
            EditCategoryPage(
                editCategoryState = editCategoryState,
                updateCategory = mainViewModel::updateCategory,
                onDelete = mainViewModel::deleteCategory
            )
        }

        composable(route = Screens.SETUP_SCREEN) {
            SettingsPage(mainViewModel = mainViewModel)
        }
    }
}