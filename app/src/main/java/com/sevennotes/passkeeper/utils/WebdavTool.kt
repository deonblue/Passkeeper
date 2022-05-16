package com.sevennotes.passkeeper.utils

import java.io.InputStream
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine

object WebdavTool {
  var url: String = ""
  val sardine: OkHttpSardine = OkHttpSardine()

  fun initSardine(userName: String, password: String, url: String) {
    this.url = url
    sardine.setCredentials(userName, password)
  }

  fun createDir(dirPath: String) {
    if (!checkExist(dirPath)) {
      val path = url + dirPath
      sardine.createDirectory(path)
    }
  }

  fun checkExist(dirPath: String): Boolean {
    val path = url + dirPath
    sardine.exists(path)
    return sardine.exists(path)
  }

  fun uploadByteArray(fileName: String, dirPath: String, data: ByteArray) {
    val fileDir = "${url}${dirPath}/$fileName"
    createDir(dirPath)
    sardine.put(fileDir, data)
  }

  fun readFile(filePath: String): InputStream {
    val path = "${url}${filePath}"
    return sardine.get(path)
  }
}