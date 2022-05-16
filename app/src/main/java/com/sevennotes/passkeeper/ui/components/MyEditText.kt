package com.sevennotes.passkeeper.ui.components

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyEditText(
    modifier: Modifier = Modifier,
    value: String,
    hint: String? = null,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false
) {
    var getFocus by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.padding(5.dp)
    ) {
        Box(
            modifier = isOnFocus(getFocus = getFocus)
                .fillMaxWidth(),
        ) {
            Column {
                AnimatedVisibility(
                    modifier = Modifier.offset(x = 10.dp, y = 5.dp),
                    visible = value != "" && hint != null
                ) {
                    Text(text = hint!!, fontSize = 12.sp)
                }
                AndroidView(
                    factory = { ctx ->
                        EditText(ctx).apply {
                            this.hint = hint
                            this.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            this.addTextChangedListener {
                                it?.let {
                                    if (it.toString() != value) {
                                        onValueChange(it.toString())
                                    }
                                }
                            }
                            this.background = null
                            this.setOnFocusChangeListener { _, hasFocus ->
                                getFocus = hasFocus
                            }
                            this.isSingleLine = singleLine
                        }
                    },
                    modifier = Modifier.padding(horizontal = 5.dp),
                    update = {
                        it.setText(value.toCharArray(), 0, value.length)
                    }
                )
            }
        }
    }
}

@Composable
fun isOnFocus(getFocus: Boolean) = Modifier.border(
    width = 2.dp,
    color = if (getFocus) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.5f),
    shape = RoundedCornerShape(5.dp)
)