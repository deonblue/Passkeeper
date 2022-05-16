package com.sevennotes.passkeeper.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sevennotes.passkeeper.config.passIcons
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.ui.viewmodel.EditCategoryState

@Composable
fun EditCategoryPage(
    editCategoryState: EditCategoryState,
    updateCategory: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    val categoryList = editCategoryState.categoryList?.observeAsState()
    var shouldDelete by remember { mutableStateOf(Category()) }
    var confirmDelete by remember { mutableStateOf(false) }
    Column {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            categoryList?.let { list ->
                items(list.value!!) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = {
                            shouldDelete = category
                            confirmDelete = true
                        },
                        updateCategory = updateCategory
                    )
                }
            }
        }
    }
    if (confirmDelete) {
        ConfirmDialog(
            tittle = "该项目下的所有密码也将一并删除,确定删除这一项吗?",
            onDismissRequest = { confirmDelete = false }
        ) {
            confirmDelete = false
            onDelete(shouldDelete)
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit,
    updateCategory: (Category) -> Unit
) {
    val img = passIcons().find { it.id == category.img }?.img ?: passIcons()[0].img
    var isIconSelecte by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = MaterialTheme.colors.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDelete() }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
            }
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .clickable { isIconSelecte = !isIconSelecte },
                painter = img,
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(2.dp))
            OutlinedTextField(
                value = category.name,
                onValueChange = { updateCategory(category.copy(name = it)) })
        }
        if (isIconSelecte) {
            ImageSelector(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                currentImg = category.img,
                onSelectChange = {
                    updateCategory(category.copy(img = it))
                    isIconSelecte = !isIconSelecte
                }
            )
        }
        Divider()
    }
}