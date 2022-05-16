package com.sevennotes.passkeeper.ui.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sevennotes.passkeeper.ui.theme.PasskeeperTheme
import com.sevennotes.passkeeper.ui.theme.lightColorPalette
import com.sevennotes.passkeeper.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainView(
    mainViewModel: MainViewModel,
    cm: android.content.ClipboardManager
) {
    val navController = rememberNavController()
    val destination = navController.currentBackStackEntryAsState()
    val currentRoute = destination.value?.destination?.route
    var searchStr by remember { mutableStateOf("") }
    var confirmDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }
    val isLogin = mainViewModel.isLogin.collectAsState()
    val isLoading = mainViewModel.isLoading.collectAsState().value
    val primaryColor = mainViewModel.primaryColor.collectAsState().value
    PasskeeperTheme(
        colors = lightColorPalette(
            primaryColor,
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            if (isLogin.value) {
                val kc = LocalSoftwareKeyboardController.current
                kc?.hide()
                Scaffold(
                    topBar = {
                        when (currentRoute) {
                            Screens.MAIN_SCREEN -> TopBar(
                                tittle = "密码管家",
                                searchStr = searchStr,
                                isDeleteMode = isDeleteMode,
                                onSearch = { searchStr = it },
                                onEditMode = {
                                    isDeleteMode = true
                                    mainViewModel.listEditable(true)
                                },
                                onDelete = { confirmDialog = true },
                                onDeleteCancel = {
                                    isDeleteMode = false
                                    mainViewModel.listEditable(false)
                                },
                                onClearSearch = { searchStr = "" },
                                onSetup = {
                                    navController.navigate(Screens.SETUP_SCREEN)
                                },
                                onEditCategory = {
                                    navController.navigate(Screens.CATE_SCREEN)
                                }
                            )
                            Screens.ADD_SCREEN -> AddBar(
                                onBackClick = { navController.popBackStack() },
                                onSaveClick = {
                                    mainViewModel.insertNewPassword()
                                    navController.popBackStack()
                                }
                            )
                            Screens.CATE_SCREEN -> CateBar(
                                onBackClick = { navController.popBackStack() },
                                onAddClick = { mainViewModel.insertCategory() }
                            )
                            Screens.SETUP_SCREEN -> SettingsBar(
                                isLoading = isLoading,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == Screens.MAIN_SCREEN) {
                            mainViewModel.updateNewPassword()
                            FloatingButton { navController.navigate(Screens.ADD_SCREEN) }
                        }
                    },
                ) {
                    if (confirmDialog) {
                        ConfirmDialog(
                            tittle = "确定要删除吗?",
                            onDismissRequest = { confirmDialog = false }
                        ) {
                            confirmDialog = false
                            isDeleteMode = false
                            mainViewModel.listEditable(false)
                            mainViewModel.deletePasswordItems()
                        }
                    }
                    MainNavHost(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        startDestination = Screens.MAIN_SCREEN,
                        searchStr = searchStr,
                        cm = cm,
                    )
                }
            } else {
                LoginPage(mainViewModel)
            }
        }
    }
}

@Composable
fun CateBar(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "类别管理") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "back")
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
            }
        },
    )
}

@Composable
fun SettingsBar(
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "设置选项") },
        navigationIcon = {
            IconButton(
                enabled = !isLoading,
                onClick = onBackClick
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "back")
            }
        },
    )
}

@Composable
fun AddBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "编辑") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "back")
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(imageVector = Icons.Filled.Done, contentDescription = "save")
            }
        },
    )
}

@Composable
fun TopBar(
    tittle: String,
    searchStr: String = "",
    isDeleteMode: Boolean,
    onSearch: (String) -> Unit,
    onEditMode: () -> Unit,
    onDelete: () -> Unit,
    onDeleteCancel: () -> Unit,
    onClearSearch: () -> Unit,
    onSetup: () -> Unit,
    onEditCategory: () -> Unit
) {
    var isSearchMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (isSearchMode) {
        TopAppBar {
            TextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                value = searchStr,
                onValueChange = onSearch,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        onClearSearch()
                        isSearchMode = false
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "exit")
                    }
                }
            )
            SideEffect { focusRequester.requestFocus() }
        }
    } else {
        TopAppBar(
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .clickable { onEditCategory() },
                        text = tittle,
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        modifier = Modifier.size(10.dp),
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onSetup() }) {
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "menu")
                }
            },
            actions = {
                if (isDeleteMode) {
                    IconButton(onClick = {
                        onDeleteCancel()
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "close")
                    }
                    IconButton(onClick = {
                        onDelete()
                    }) {
                        Icon(imageVector = Icons.Filled.Done, contentDescription = "confirm")
                    }
                } else {
                    IconButton(onClick = { isSearchMode = true }) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "search")
                    }
                    IconButton(onClick = {
                        onEditMode()
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                    }
                }
            },
        )
    }
}

@Composable
fun FloatingButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidthInPx = with(LocalDensity.current) { screenWidth.toPx() }
    val screenHeightInPx = with(LocalDensity.current) { screenHeight.toPx() }
    val minXpx = with(LocalDensity.current) { 10.dp.toPx() }
    val minYpx = with(LocalDensity.current) { 80.dp.toPx() }
    val maxXpx = screenWidthInPx - with(LocalDensity.current) { 80.dp.toPx() }
    val maxYpx = screenHeightInPx - with(LocalDensity.current) { 80.dp.toPx() }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    FloatingActionButton(
        modifier = modifier
            .size(65.dp)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .onGloballyPositioned {
                val sp = it.positionInWindow()
                if (sp.x < minXpx) offsetX -= sp.x - minXpx
                if (sp.x > maxXpx) offsetX -= sp.x - maxXpx
                if (sp.y < minYpx) offsetY -= sp.y - minYpx
                if (sp.y > maxYpx) offsetY -= sp.y - maxYpx
            }
            .pointerInput("floatButton") {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        backgroundColor = MaterialTheme.colors.primary,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview
@Composable
fun TopPreview() {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "abc"
            )
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    )
}