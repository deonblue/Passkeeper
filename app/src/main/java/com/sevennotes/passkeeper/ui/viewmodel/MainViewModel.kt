package com.sevennotes.passkeeper.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sevennotes.passkeeper.config.*
import com.sevennotes.passkeeper.data.respository.PasswordRepository
import com.sevennotes.passkeeper.models.Category
import com.sevennotes.passkeeper.models.Password
import com.sevennotes.passkeeper.utils.DESCrypt
import com.sevennotes.passkeeper.utils.WebdavTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.*

data class Backup(
    val password: String,
    val passList: List<Password>?,
    var categoryList: List<Category>?
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

data class PassListState(
    var passList: LiveData<List<Password>>? = null,
    var categoryList: LiveData<List<Category>>? = null,
    var collectedList: MutableList<Password> = mutableStateListOf(),
    var isEditMode: MutableState<Boolean> = mutableStateOf(false)
)

data class SettingsState(
    val busy: Boolean = false,
    var webDavUrl: String = "https://dav.jianguoyun.com/dav/",
    var webDavUsername: String = "",
    var webDavPassword: String = "",
    var dirName: String = "PassKeeper"
)

data class AddPageState(
    var categoryList: LiveData<List<Category>>? = null,
    var name: MutableState<String> = mutableStateOf(""),
    var url: MutableState<String> = mutableStateOf(""),
    var account: MutableState<String> = mutableStateOf(""),
    var password: MutableState<String> = mutableStateOf(""),
    var note: MutableState<String> = mutableStateOf(""),
    var categoryId: MutableState<Int> = mutableStateOf(0),
)

data class EditCategoryState(
    val categoryList: LiveData<List<Category>>? = null,
    val newCategory: Category = Category()
)

class MainViewModel(
    private val passwordRepository: PasswordRepository,
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
) : ViewModel() {

    var passListState: PassListState = PassListState()
    var addPageState = AddPageState()
    private val _editCategoryState = MutableStateFlow(EditCategoryState())
    val editCategoryState = _editCategoryState.asStateFlow()
    private var _loginPassword = ""
    private var _firstTimeLogin = MutableStateFlow(false)
    var firstTimeLogin = _firstTimeLogin.asStateFlow()
    private var _isLogin = MutableStateFlow(false)
    var isLogin = _isLogin.asStateFlow()
    private var _primaryColor = MutableStateFlow(Color(0x0, 0xBC, 0xD4, 0xFF))
    val primaryColor = _primaryColor.asStateFlow()
    private var backup: Backup? = null
    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    init {
        refreshAll()
    }

    fun setPrimaryColor(color: Color) {
        viewModelScope.launch {
            _primaryColor.update { color }
            sharedPreferences.edit {
                putFloat(RED, color.red)
                putFloat(GREEN, color.green)
                putFloat(BLUE, color.blue)
                commit()
            }
        }
    }

    private fun readSettings() {
        val r = sharedPreferences.getFloat(RED, -1F)
        val g = sharedPreferences.getFloat(GREEN, -1F)
        val b = sharedPreferences.getFloat(BLUE, -1F)
        if (r == -1f || g == -1f || b == -1f) return else _primaryColor.update { Color(r, g, b) }
        viewModelScope.launch {
            _settingsState.update {
                it.copy(
                    webDavUrl = sharedPreferences.getString(WEB_DAV_URL, "https://dav.jianguoyun.com/dav/")!!,
                    webDavUsername = sharedPreferences.getString(WEB_DAV_USERNAME, "")!!,
                    webDavPassword = sharedPreferences.getString(WEB_DAV_PASSWORD, "")!!,
                    dirName = sharedPreferences.getString(DIRNAME,"PassKeeper")!!
                )
            }
        }
    }

    private fun refreshAll() {
        viewModelScope.launch {
            readSettings()
            if (readPassword()) {
                val categoryList = passwordRepository.getAllCategories()
                passListState.categoryList = categoryList
                addPageState.categoryList = categoryList
                _editCategoryState.update {
                    it.copy(categoryList = categoryList)
                }
                passListState.passList = passwordRepository.getAllPasswords()
            } else {
                val catesNum = passwordRepository.getCategoryNumbers()
                if (catesNum == 0) insertCates()
            }
        }
    }

    private fun readPassword(): Boolean {
        val passwordMD5 = sharedPreferences.getString(PASSWORD_MD5, "")
        return if (passwordMD5 == "") {
            _firstTimeLogin.update { true }
            false
        } else {
            true
        }
    }

    fun changeLoginPassword(oldPassword: String, newPassword: String): String {
        val oldMD5 = sharedPreferences.getString(PASSWORD_MD5, "")
        if (DESCrypt.MD5(oldPassword) != oldMD5)
            return "旧密码不正确"
        else {
            _loginPassword = newPassword
            val md5 = DESCrypt.MD5(newPassword)
            sharedPreferences.edit {
                putString(PASSWORD_MD5, md5)
                commit()
            }
            exchangeAllPasswords(oldPassword, newPassword)
            return "修改成功"
        }
    }

    private fun exchangeAllPasswords(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            passListState.passList?.value?.forEach {
                val newP = it.copy(
                    account = DESCrypt.encrypt(
                        DESCrypt.decrypt(it.account, oldPassword),
                        newPassword
                    ),
                    password = DESCrypt.encrypt(
                        DESCrypt.decrypt(it.password, oldPassword),
                        newPassword
                    ),
                )
                passwordRepository.updatePassword(newP)
            }
            refreshAll()
            _isLoading.update { false }
        }
    }

    fun decodeString(content: String): String? {
        return if (_loginPassword == "") {
            _isLogin.update { false }
            null
        } else {
            DESCrypt.decrypt(content, _loginPassword)
        }
    }

    private fun encodeString(content: String): String? {
        return if (_loginPassword == "") {
            _isLogin.update { false }
            null
        } else {
            DESCrypt.encrypt(content, _loginPassword)
        }
    }

    fun setupLoginPassword(password: String) {
        viewModelScope.launch {
            _loginPassword = password
            val md5 = DESCrypt.MD5(password)
            sharedPreferences.edit {
                putString(PASSWORD_MD5, md5)
                commit()
            }
            _isLogin.update { true }
            refreshAll()
        }
    }

    fun checkLoginPassword(password: String): Boolean {
        val md5 = sharedPreferences.getString(PASSWORD_MD5, "")
        return if (DESCrypt.MD5(password) == md5) {
            _loginPassword = password
            _isLogin.update { true }
            true
        } else {
            false
        }
    }

    //@ColumnInfo var name: String = "",
//@ColumnInfo var url: String = "",
//@ColumnInfo var account: String = "",
//@ColumnInfo var password: String = "",
//@ColumnInfo var note: String = "",
//@ColumnInfo var categoryId: Int = 0
    fun updateNewPassword(password: Password = Password()) {
        viewModelScope.launch {
            addPageState.apply {
                this.name.value = password.name
                this.url.value = password.url
                this.account.value = password.account
                this.password.value = password.password
                this.note.value = password.note
                this.categoryId.value = password.categoryId
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            passwordRepository.updateCategory(category)
        }
    }

    fun insertCategory() {
        viewModelScope.launch {
            passwordRepository.insertCategory(category = Category(name = "新建类别"))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            passwordRepository.deleteCategory(category)
        }
    }

    //@ColumnInfo var name: String = "",
//@ColumnInfo var url: String = "",
//@ColumnInfo var account: String = "",
//@ColumnInfo var password: String = "",
//@ColumnInfo var note: String = "",
//@ColumnInfo var categoryId: Int = 0
    fun insertNewPassword() {
        var newPassword = Password()
        addPageState.apply {
            newPassword.name = this.name.value
            newPassword.url = this.url.value
            newPassword.account = this.account.value
            newPassword.password = this.password.value
            newPassword.note = this.note.value
            newPassword.categoryId = this.categoryId.value
        }
        val encodeAccount = encodeString(newPassword.account)
        val encodePassword = encodeString(newPassword.password)
        if (encodeAccount != null && encodePassword != null) {
            viewModelScope.launch {
                newPassword = newPassword.copy(
                    account = encodeAccount,
                    password = encodePassword
                )
                if (newPassword.id == null) {
                    passwordRepository.insertPassword(newPassword)
                } else {
                    passwordRepository.updatePassword(newPassword)
                }
            }
        }
    }

    fun listEditable(switch: Boolean) {
        viewModelScope.launch {
            if (switch) {
                passListState.collectedList.clear()
            }
            passListState.isEditMode.value = switch
        }
    }

    fun deletePasswordItems() {
        viewModelScope.launch {
            passListState.collectedList.forEach {
                passwordRepository.deletePassword(it)
            }
        }
    }

    fun getFileDir(): String {
        return context.getExternalFilesDir(null)?.absolutePath ?: ""
    }

    fun changeUrl(url: String) {
        viewModelScope.launch {
            _settingsState.update {
                it.copy(webDavUrl = url)
            }
          sharedPreferences.edit {
              putString(WEB_DAV_URL, url)
              commit()
          }
        }
    }
    fun changeUser(user: String) {
        viewModelScope.launch {
            _settingsState.update {
                it.copy(webDavUsername = user)
            }
            sharedPreferences.edit {
                putString(WEB_DAV_USERNAME, user)
                commit()
            }
        }
    }

    fun changePass(pass: String) {
        viewModelScope.launch {
            _settingsState.update {
                it.copy(webDavPassword = pass)
            }
            sharedPreferences.edit {
                putString(WEB_DAV_PASSWORD, pass)
                commit()
            }
        }
    }
    fun changeDir(dir: String) {
        viewModelScope.launch {
            _settingsState.update {
                it.copy(dirName = dir)
            }
            sharedPreferences.edit {
                putString(DIRNAME, dir)
                commit()
            }
        }
    }

    fun onNetBackup(finish: (String) -> Unit) {
        viewModelScope.launch {
            val backup = Backup(
                sharedPreferences.getString(PASSWORD_MD5, "")!!,
                passListState.passList?.value,
                passListState.categoryList?.value
            )
            WebdavTool.initSardine(
                userName = settingsState.value.webDavUsername,
                password = settingsState.value.webDavPassword,
                url = settingsState.value.webDavUrl
            )
            launch(Dispatchers.IO) {
                _settingsState.update { it.copy(busy = true) }
                try {
                    ByteArrayOutputStream().use { bos ->
                        ObjectOutputStream(bos).use { oos ->
                            oos.writeObject(backup)
                        }
                        WebdavTool.uploadByteArray(
                            fileName = "backup.pass",
                            dirPath = settingsState.value.dirName,
                            data = bos.toByteArray()
                        )
                    }
                    finish("上传成功!")
                } catch (e: Exception) {
                    finish("上传失败! ${e.message}")
                }
                _settingsState.update { it.copy(busy = false) }
            }
        }
    }

    fun onNetRecovery(finish: (String) -> Unit) {
        WebdavTool.initSardine(
            userName = settingsState.value.webDavUsername,
            password = settingsState.value.webDavPassword,
            url = settingsState.value.webDavUrl
        )
        viewModelScope.launch {
            launch(Dispatchers.IO) {
            _settingsState.update { it.copy(busy = true) }
                try {
                    val inputs = WebdavTool.readFile("${settingsState.value.dirName}/backup.pass")
                    inputs.use { ins ->
                        ObjectInputStream(ins).use { ois ->
                            ois.readObject().also {
                                backup = it as Backup
                            }
                        }
                    }
                    finish(READ_SUCCESS)
                } catch (e: Exception) {
                    finish("读取失败 ${e.message}")
                }
            _settingsState.update { it.copy(busy = false) }
            }
        }
    }


//    fun onNetBackup() {
//
//    }
//
//    fun onNetRecovery() {
//
//    }

    fun backupFile(uri: Uri?, context: Context) {
        val backup = Backup(
            sharedPreferences.getString(PASSWORD_MD5, "")!!,
            passListState.passList?.value,
            passListState.categoryList?.value
        )
        try {
            uri?.let { i ->
                context.contentResolver.openFileDescriptor(i, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { fos ->
                        ObjectOutputStream(fos).use { oos ->
                            oos.writeObject(backup)
                        }
                    }
                }
                Toast.makeText(context, "存储成功", Toast.LENGTH_SHORT).show()
            }
            if (uri == null) {
                Toast.makeText(context, "文件地址错误", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "存储失败${e.message}", Toast.LENGTH_SHORT).show()
        }
//        return try {
//            val filedir = getFileDir() + "/backup.pass"
//            val file = File(filedir)
//            if (file.exists()) file.delete()
//            file.createNewFile()
//            val fileOutputStream = FileOutputStream(file, true)
//            val objectOutputStream = ObjectOutputStream(fileOutputStream)
//            objectOutputStream.writeObject(backup)
//            objectOutputStream.close()
//            fileOutputStream.close()
//            "备份成功"
//        } catch (e: Exception) {
//            "备份失败 ${e.message}"
//        }
    }

    fun readBackupFile(uri: Uri?): String {
        try {
          uri?.let { i ->
              context.contentResolver.openFileDescriptor(i, "r")?.use {
                  FileInputStream(it.fileDescriptor).use { fis ->
                      ObjectInputStream(fis).use { ois ->
                          ois.readObject().also { o ->
                              backup = o as Backup
                          }
                      }
                  }
              }
              return READ_SUCCESS
          }
          if (uri == null) {
              return "文件不存在"
          }
        } catch (e: Exception) {
          return "读取失败"
        }
        return "读取失败"
//        var filedir = ""
//        if (uri != null) {
//            val url = uri.encodedPath
//            url?.let {
//                if (it.endsWith(".pass")) {
//                    filedir = it
//                } else {
//                    return "文件格式不正确"
//                }
//            }
//        } else {
//            filedir = getFileDir() + "/backup.pass"
//        }
//        val file = File(filedir)
//        if (file.exists()) {
//            val fileInputStream = FileInputStream(file)
//            val objectInputStream = ObjectInputStream(fileInputStream)
//            try {
//                objectInputStream.readObject().also {
//                    backup = it as Backup
//                }
//            } catch (e: Exception) {
//                return "读取失败   ${e.message}"
//            } finally {
//                objectInputStream.close()
//                fileInputStream.close()
//            }
//        } else {
//            return "文件不存在$filedir"
//        }
//        return READ_SUCCESS
    }

    fun checkBackupPassword(password: String): String {
        backup?.let {
            return if (it.password == DESCrypt.MD5(password)) {
                recoveryBackup(password)
                READ_SUCCESS
            } else
                "密码错误!"
        }
        return "密码错误!"
    }

    private fun recoveryBackup(password: String) {
        viewModelScope.launch {
            passwordRepository.clearAll()
            backup?.categoryList?.forEach {
                passwordRepository.insertCategory(it)
            }
            backup?.passList?.forEach {
                passwordRepository.insertPassword(it)
            }
            setupLoginPassword(password)
        }
    }

    private suspend fun insertCates() {
        passwordRepository.insertCategory(Category(name = "网站论坛", img = 10))
        passwordRepository.insertCategory(Category(name = "电子邮箱", img = 2))
        passwordRepository.insertCategory(Category(name = "银行账户", img = 6))
        passwordRepository.insertCategory(Category(name = "游戏账户", img = 7))
        passwordRepository.insertCategory(Category(name = "其他账户", img = 0))
    }

    companion object {
        fun provideFactory(
            passwordRepository: PasswordRepository,
            sharedPreferences: SharedPreferences,
            context: Context,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(passwordRepository, sharedPreferences, context) as T
            }
        }
    }
}