package com.sevennotes.passkeeper.ui.pages

import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevennotes.passkeeper.config.DIVIDER_COLOR
import com.sevennotes.passkeeper.config.READ_SUCCESS
import com.sevennotes.passkeeper.ui.components.PasswordInput
import com.sevennotes.passkeeper.ui.components.validTest
import com.sevennotes.passkeeper.ui.viewmodel.MainViewModel
import com.sevennotes.passkeeper.ui.viewmodel.SettingsState
import com.sevennotes.passkeeper.utils.contentColor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsPage(
  mainViewModel: MainViewModel,
) {
  val settingsState = mainViewModel.settingsState.collectAsState().value
  var colorPickerVis by remember { mutableStateOf(false) }
  var message by remember { mutableStateOf("") }
  var messageshow by remember { mutableStateOf(false) }
  var editPassword by remember { mutableStateOf(false) }
  var backupPassword by remember { mutableStateOf("") }
  var inputPasswordDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val keyboard = LocalSoftwareKeyboardController.current
  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = {
      val test = mainViewModel.readBackupFile(it)
      if (test == READ_SUCCESS)
        inputPasswordDialog = true
      else
        Toast.makeText(context, test, Toast.LENGTH_LONG).show()
    }
  )
  val slauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument(),
    onResult = {
      mainViewModel.backupFile(it, context)
    }
  )
  if (settingsState.busy) {
    AlertDialog(
      onDismissRequest = {},
      buttons = { Spacer(modifier = Modifier.height(30.dp)) },
      title = { androidx.compose.material3.Text("请求网络中...") }
    )
  }
  if (messageshow) {
    SimpleMsgDialog(text = message) {
      messageshow = false
    }
  }
  if (inputPasswordDialog) {
    AlertDialog(
      onDismissRequest = { },
      title = { Text("请输入密码:") },
      text = {
        PasswordInput(
          label = "",
          value = backupPassword,
          notValid = !validTest(backupPassword),
          onValueChange = { backupPassword = it }
        )
      },
      buttons = {
        TextButton(
          modifier = Modifier.fillMaxWidth(),
          enabled = backupPassword != "" && validTest(backupPassword),
          onClick = {
            val res = mainViewModel.checkBackupPassword(backupPassword)
            Toast.makeText(context, res, Toast.LENGTH_LONG).show()
            inputPasswordDialog = false
            backupPassword = ""
          }
        ) {
          Text(text = "确定")
        }
      }
    )
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Spacer(modifier = Modifier.height(10.dp))
    var serverSetting by remember { mutableStateOf(false) }
    SettingsCell(title = "webCav服务器设置") {
      keyboard?.hide()
      serverSetting = !serverSetting
    }
    AnimatedVisibility(serverSetting) {
      Column {
        ItemCell {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settingsState.webDavUrl,
            onValueChange = mainViewModel::changeUrl,
            label = { Text("webCav数据服务器地址") }
          )
        }
        ItemCell {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settingsState.webDavUsername,
            onValueChange = mainViewModel::changeUser,
            label = { androidx.compose.material3.Text("webCav账号") }
          )
        }
        ItemCell {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settingsState.webDavPassword,
            onValueChange = mainViewModel::changePass,
            label = { androidx.compose.material3.Text("webCav密码") }
          )
        }
        ItemCell {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = settingsState.dirName,
            onValueChange = mainViewModel::changeDir,
            label = { androidx.compose.material3.Text("存储文件夹") }
          )
        }
      }
    }
    SettingsCell(title = "网络数据备份") {
      mainViewModel.onNetBackup {
        messageshow = true
        message = it
      }
    }
    SettingsCell(title = "网络数据还原") {
      mainViewModel.onNetRecovery {
        if (it == READ_SUCCESS)
          inputPasswordDialog = true
        else {
          message = it
          messageshow = true
        }
      }
    }
    SettingsCell(title = "本地数据备份") { slauncher.launch("backup.pass") }
    SettingsCell(title = "本地数据还原") { launcher.launch("*/*") }
    SettingsCell(title = "修改密码") { editPassword = !editPassword }
    if (editPassword) {
      var oldPassword by remember { mutableStateOf("") }
      var newPassword by remember { mutableStateOf("") }
      var confirmPassword by remember { mutableStateOf("") }
      val notNull = oldPassword != "" && newPassword != "" && confirmPassword != ""
      val buttonEnable =
        notNull && validTest(oldPassword) && validTest(newPassword) && validTest(
          confirmPassword
        )
      AlertDialog(
        onDismissRequest = { editPassword = false },
        title = { Text("修改开屏密码") },
        text = {
          Column {
            PasswordInput(
              label = "输入旧密码",
              value = oldPassword,
              notValid = !validTest(oldPassword),
              onValueChange = { oldPassword = it }
            )
            PasswordInput(
              label = "输入新密码",
              value = newPassword,
              notValid = !validTest(newPassword),
              onValueChange = { newPassword = it }
            )
            PasswordInput(
              label = "确认新密码",
              value = confirmPassword,
              notValid = !validTest(confirmPassword),
              onValueChange = { confirmPassword = it }
            )
          }
        },
        buttons = {
          TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonEnable,
            onClick = {
              if (newPassword != confirmPassword)
                Toast.makeText(context, "新密码输入不一致", Toast.LENGTH_SHORT).show()
              else {
                val res =
                  mainViewModel.changeLoginPassword(oldPassword, newPassword)
                if (res == "修改成功") editPassword = false
                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
              }
            }
          ) {
            Text("确定")
          }
        }
      )
    }
    SettingsCell(title = "主题颜色") {
      colorPickerVis = !colorPickerVis
    }
    if (colorPickerVis) {
      Spacer(modifier = Modifier.height(10.dp))
      ColorSelector(color = mainViewModel.primaryColor.collectAsState().value) {
        mainViewModel.setPrimaryColor(it)
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun SimpleMsgDialog(
  text: String,
  onConfirm: () -> Unit
) {
  AlertDialog(
    title = { Text(text) },
    onDismissRequest = onConfirm,
    buttons = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = { onConfirm() }) {
          Text(text = "确定", color = Color.Red)
        }
      }
    }
  )
}

@Composable
fun ItemCell(
  modifier: Modifier = Modifier,
  rowHeight: Int = 60,
  hArrangement: Arrangement.Horizontal = Arrangement.Start,
  content: @Composable RowScope.() -> Unit
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(rowHeight.dp)
      .padding(horizontal = 10.dp),
    horizontalArrangement = hArrangement,
    verticalAlignment = Alignment.CenterVertically,
    content = content
  )
  Divider(
    Modifier
      .fillMaxWidth()
      .height(1.dp), color = Color(DIVIDER_COLOR)
  )
}

@Composable
fun SettingsCell(
  title: String,
  onClick: () -> Unit
) {
  ItemCell(
    modifier = Modifier.clickable { onClick() },
  ) {
    androidx.compose.material3.Text(
      text = title,
      modifier = Modifier.weight(1f),
      fontSize = 16.sp
    )
    androidx.compose.material3.Icon(
      imageVector = Icons.Filled.KeyboardArrowRight,
      contentDescription = null
    )
  }
}

@Composable
fun ColorSelector(
  modifier: Modifier = Modifier,
  color: Color,
  onColorChange: (Color) -> Unit
) {
  Column(modifier = modifier) {
    ColorSlider(
      title = "R:",
      value = color.red,
      onValueChange = { onColorChange(color.copy(red = it)) }
    )
    ColorSlider(
      title = "G:",
      value = color.green,
      onValueChange = { onColorChange(color.copy(green = it)) }
    )
    ColorSlider(
      title = "B:",
      value = color.blue,
      onValueChange = { onColorChange(color.copy(blue = it)) }
    )
  }
}

@Composable
fun ColorSlider(title: String, value: Float, onValueChange: (Float) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = title)
    Spacer(modifier = Modifier.width(5.dp))
    Slider(value = value, onValueChange = onValueChange)
  }
}