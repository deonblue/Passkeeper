package com.sevennotes.passkeeper.ui.pages

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sevennotes.passkeeper.config.passIcons
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.ui.viewmodel.EditCategoryState

@Composable
fun AddCategoryPage(
    editCategoryState: EditCategoryState,
    updateCategory: (Category) -> Unit
) {
    val newCategory = editCategoryState.newCategory

    Column {
        TextField(value = newCategory.name, onValueChange = {
            updateCategory(newCategory.copy(name = it))
        })
        ImageSelector(currentImg = newCategory.img, onSelectChange = {
            updateCategory(newCategory.copy(img = it))
        })
    }
}

@Composable
fun ImageSelector(
    modifier: Modifier = Modifier,
    currentImg: Int,
    onSelectChange: (Int) -> Unit
) {
    val iconLists = passIcons().divide(6)
    val border = Modifier.border(2.dp, Color.Green)
    Column(modifier = modifier) {
        iconLists.forEach { plist ->
            Row {
                plist.forEach {
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                        .clickable { onSelectChange(it.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = (if (it.id == currentImg) border else Modifier)
                                .size(50.dp),
                            painter = it.img,
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun IconListPreview() {
    ImageSelector(currentImg = 1, onSelectChange = {})
}

fun <T> List<T>.divide(num: Int): List<List<T>> {
    val newList: MutableList<List<T>> = mutableListOf()
    val tempList: MutableList<T> = mutableListOf()
    for (i in this.indices) {
        tempList.add(this[i])
        if ((i + 1) % num == 0) {
            newList.add(tempList.toList())
            tempList.clear()
        }
    }
    if (tempList.isNotEmpty()) newList.add(tempList.toList())
    return newList.toList()
}