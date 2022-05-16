package com.sevennotes.passkeeper.ui.pages

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sevennotes.passkeeper.config.READ_SUCCESS
import com.sevennotes.passkeeper.ui.components.PasswordInput
import com.sevennotes.passkeeper.ui.components.validTest
import com.sevennotes.passkeeper.ui.viewmodel.MainViewModel

@Composable
fun LoginPage(
    mainViewModel: MainViewModel
) {
    var password by remember { mutableStateOf("") }
    val firstLabel = if (validTest(password)) "输入密码" else "密码长度最少为8位"
    val firstLogin = mainViewModel.firstTimeLogin.collectAsState()
    val context = LocalContext.current
    var confirmDialog by remember { mutableStateOf(false) }

    if (confirmDialog) {
        AlertDialog(
            onDismissRequest = { confirmDialog = false },
            title = { Text("请牢记该密码, 它将是你恢复数据的唯一凭证!") },
            buttons = {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { mainViewModel.setupLoginPassword(password) }
                ) {
                    Text(text = "知道了")
                }
            }
        )
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (firstLogin.value) {
            var confirmPass by remember { mutableStateOf("") }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val secondLabel = if (validTest(confirmPass)) "再次输入密码" else "密码长度最少为8位"
                Text(text = "首次登陆需要设置开屏密码")
                Spacer(modifier = Modifier.height(10.dp))
                PasswordInput(
                    label = firstLabel,
                    value = password,
                    notValid = !validTest(password),
                    onValueChange = { password = it }
                )
                PasswordInput(
                    label = secondLabel,
                    value = confirmPass,
                    notValid = !validTest(confirmPass),
                    onValueChange = { confirmPass = it },
                )
                Spacer(modifier = Modifier.height(20.dp))
                val buttonEnable = password != "" && confirmPass != "" && validTest(password) && validTest(confirmPass)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = buttonEnable,
                    onClick = {
                        if (password == confirmPass)
                            confirmDialog = true
                        else
                            Toast.makeText(context, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "登录")
                }
//                Spacer(modifier = Modifier.height(30.dp))
//                val launch = recoveryBacup(mainViewModel = mainViewModel)
//                TextButton(
//                    onClick = { launch() }
//                ) {
//                    Text(text = "恢复备份", color = Color.Green)
//                }
            }
        } else {
            Column {
                PasswordInput(
                    label = firstLabel,
                    value = password,
                    notValid = !validTest(password),
                    onValueChange = { password = it },
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val res = mainViewModel.checkLoginPassword(password)
                        if (res)
                            Toast.makeText(context, "登陆成功!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context, "登陆失败,密码不正确", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "登录")
                }
            }
        }
    }
}

