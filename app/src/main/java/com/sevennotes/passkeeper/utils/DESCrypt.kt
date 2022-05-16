package com.sevennotes.passkeeper.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.security.Key
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

object DESCrypt {
    @SuppressLint("GetInstance")
    fun encrypt(original: String, password: String): String {
        //创建cipher对象
        val cipher = Cipher.getInstance("DES")

        //初始化cipher(参数：加密/解密模式)
        val kf = SecretKeyFactory.getInstance("DES")
        val keySpec = DESKeySpec(password.toByteArray())

        val key: Key = kf.generateSecret(keySpec)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        //加密/解密
        val encrypt = cipher.doFinal(original.toByteArray())

        //base64加密
        return String(Base64.encode(encrypt, Base64.DEFAULT))
    }

    fun decrypt(original: String, password: String): String {
        //创建cipher对象
        val cipher = Cipher.getInstance("DES")

        //初始化cipher(参数：加密/解密模式)
        val kf = SecretKeyFactory.getInstance("DES")
        val keySpec = DESKeySpec(password.toByteArray())

        val key: Key = kf.generateSecret(keySpec)
        cipher.init(Cipher.DECRYPT_MODE, key)

        //base64解码
        val encrypt = cipher.doFinal(Base64.decode(original,Base64.DEFAULT))

        return String(encrypt)
    }

    fun MD5(content: String): String {
        try {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")//获取md5加密对象
            val digest: ByteArray = instance.digest(content.toByteArray())//对字符串加密，返回字节数组
            val sb = StringBuffer()
            for (b in digest) {
                val i: Int = b.toInt() and 0xff//获取低八位有效值
                var hexString = Integer.toHexString(i)//将整数转化为16进制
                if (hexString.length < 2) {
                    hexString = "0$hexString"//如果是一位的话，补0
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}