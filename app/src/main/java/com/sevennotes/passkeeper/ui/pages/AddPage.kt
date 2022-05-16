package com.sevennotes.passkeeper.ui.pages

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sevennotes.passkeeper.config.passIcons
import com.sevennotes.passkeeper.models.Password
import com.sevennotes.passkeeper.ui.components.MyEditText
import com.sevennotes.passkeeper.ui.viewmodel.AddPageState

@Composable
fun AddPage(
    addPageState: AddPageState,
    openEditCategory: () -> Unit
) {
    val categories = addPageState.categoryList?.observeAsState()
    val currentCateId = addPageState.categoryId.value
    if (currentCateId == 0) {
        addPageState.categoryId.value = categories!!.value!![0].id!!
    }
    var menuOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.padding(10.dp)) {
            Text("密码种类:")
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity),
                        contentColor = LocalContentColor.current.copy(LocalContentAlpha.current)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 1.dp),
                    onClick = { menuOpen = !menuOpen },
                ) {
                    val imgId = categories!!.value!!.find { it.id == currentCateId }?.img ?: 0
                    val defaultImg = passIcons()[0].img
                    Row(
                        modifier = Modifier.offset(x = (-50).dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = passIcons().find { it.id == imgId }?.img ?: defaultImg,
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            fontSize = 20.sp,
                            text = categories.value!!.find { it.id == currentCateId }?.name ?: ""
                        )
                    }
                }
                Spacer(modifier = Modifier.width(30.dp))
                Button(onClick = openEditCategory) {
                    Text("类别管理")
                }
            }

            if (menuOpen) {
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    categories!!.value!!.forEach {
                        DropdownMenuItem(onClick = {
                            addPageState.categoryId.value = it.id!!
                            menuOpen = !menuOpen
                        }) { Text(it.name) }
                    }
                }
            }
        }
        InfoItem(
            tittle = "标题",
            content = addPageState.name.value,
            contentChange = {
//                updatePassword(addPageState.newPassword.copy(name = it))
                addPageState.name.value = it
            }
        )
        InfoItem(
            tittle = "网址",
            content = addPageState.url.value,
            contentChange = {
//                updatePassword(addPageState.newPassword.copy(url = it))
                addPageState.url.value = it
            }
        )
        InfoItem(
            tittle = "账号",
            content = addPageState.account.value,
            contentChange = {
//                updatePassword(addPageState.newPassword.copy(account = it))
                addPageState.account.value = it
            }
        )
        InfoItem(
            tittle = "密码",
            content = addPageState.password.value,
            contentChange = {
//                updatePassword(addPageState.newPassword.copy(password = it))

                addPageState.password.value = it
            }
        )
        InfoItem(
            tittle = "备注",
            content = addPageState.note.value,
            singleLine = false,
            contentChange = {
//                updatePassword(addPageState.newPassword.copy(note = it))

                addPageState.note.value = it
            }
        )
    }
}

@Composable
fun InfoItem(
    modifier: Modifier = Modifier,
    tittle: String,
    content: String,
    singleLine: Boolean = true,
    contentChange: (String) -> Unit
) {
    TextField(
        modifier = modifier
            .padding(10.dp)
            .fillMaxWidth(),
        label = { Text(tittle) },
        value = content,
        singleLine = singleLine,
        onValueChange = {
            contentChange(it)
        }
    )
}