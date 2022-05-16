package com.sevennotes.passkeeper.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevennotes.passkeeper.R
import com.sevennotes.passkeeper.config.passIcons
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.models.Password
import com.sevennotes.passkeeper.ui.viewmodel.PassListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PassList(
    modifier: Modifier = Modifier,
    passListState: PassListState,
    searchStr: String,
    cm: ClipboardManager,
    decode: (String) -> String?,
    onPasswordClick: (Password) -> Unit
) {
    val passList = passListState.passList?.observeAsState()?.value?.map {
        it.copy(
            account = decode(it.account) ?: "",
            password = decode(it.password) ?: ""
        )
    }
    val categories = passListState.categoryList?.observeAsState()
    val openState = remember { mutableStateListOf<Int>() }
    val collectedList = passListState.collectedList
    fun searched(password: Password): Boolean {
        return password.name.contains(searchStr) || password.account.contains(searchStr)
    }
    LazyColumn(modifier = modifier) {
        categories?.value?.let { cates ->
            cates.forEach { category ->
                item(category.id) {
                    PassListItem(
                        item = category,
                        numberOfitem = passList?.count { it.categoryId == category.id && searched(it) }
                            ?: 0,
                        isOpen = openState.contains(category.id),
                        onClick = {
                            if (openState.contains(category.id)) {
                                openState.remove(category.id)
                            } else {
                                category.id?.let { openState.add(it) }
                            }
                        }
                    )
                }
                if (openState.contains(category.id)) {
                    passList?.let { passes ->
                        items(passes) { password ->
                            if(password.categoryId == category.id && searched(password)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedVisibility(passListState.isEditMode.value) {
                                        Checkbox(
                                            checked = password in collectedList,
                                            onCheckedChange = {
                                                if (it)
                                                    collectedList.add(password)
                                                else
                                                    collectedList.remove(password)
                                            }
                                        )
                                    }
                                    PasswordItem(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                        item = password,
                                        cm = cm,
                                        onClick = { onPasswordClick(password) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassListItem(
    modifier: Modifier = Modifier,
    item: Category,
    numberOfitem: Int,
    isOpen: Boolean = false,
    onClick: () -> Unit = {}
) {
    val icon = passIcons().find { it.id == item.img }?.img ?: passIcons()[0].img
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colors.surface)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(modifier = Modifier.weight(1f), text = item.name)
            Text(text = "$numberOfitem")
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = if (isOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
        Divider(Modifier.background(Color.Gray))
    }
}

@Composable
fun PasswordItem(
    modifier: Modifier = Modifier,
    item: Password,
    cm: ClipboardManager,
    onClick: () -> Unit
) {

    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            InsideRow(
                name = "",
                content = if (item.name == "") "无标题" else item.name,
                onClick = onClick,
                onLongTounch = {
                    cm.setPrimaryClip(ClipData.newPlainText("Label", item.name))
                    Toast.makeText(context, "标题已复制!", Toast.LENGTH_SHORT).show()
                }
            )
            if (item.account != "") {
                InsideRow(name = "账号:", content = item.account, onClick = {
                    cm.setPrimaryClip(ClipData.newPlainText("Label", item.account))
                    Toast.makeText(context, "账号已复制!", Toast.LENGTH_SHORT).show()
                })
            }
            if (item.password != "") {
                InsideRow(
                    name = "密码:",
                    isPassword = true,
                    content = item.password,
                    onClick = {
                        cm.setPrimaryClip(ClipData.newPlainText("Label", item.password))
                        Toast.makeText(context, "密码已复制!", Toast.LENGTH_SHORT).show()
                    })
            }
            if (item.url != "") {
                InsideRow(name = "网址:", content = item.url, onClick = {
                    cm.setPrimaryClip(ClipData.newPlainText("Label", item.url))
                    Toast.makeText(context, "网址已复制!", Toast.LENGTH_SHORT).show()
                })
            }
            if (item.note != "") {
                InsideRow(name = "备注:", content = "")
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = item.note,
                    onValueChange = {},
                    readOnly = true
                )
            }
        }
    }
}

@Composable
fun InsideRow(
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    name: String,
    content: String,
    onClick: () -> Unit = {},
    onLongTounch: () -> Unit = {}
) {
    var passOpen by remember { mutableStateOf(!isPassword) }
    Box(
        modifier = modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(name) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onLongPress = { onLongTounch() }
                        )
                    },
                text = if (passOpen) content else "**********",
                fontSize = 16.sp
            )
            if (isPassword) {
                IconButton(onClick = { passOpen = !passOpen}) {
                    if (passOpen) {
                        Icon(modifier = Modifier.size(25.dp), painter = painterResource(id = R.drawable.ic_open_eye), contentDescription = null)
                    } else {
                        Icon(modifier = Modifier.size(25.dp), painter = painterResource(id = R.drawable.ic_close_eye), contentDescription = null)
                    }
                }
            }
        }
    }
}
