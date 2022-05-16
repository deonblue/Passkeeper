package com.sevennotes.passkeeper

import com.sevennotes.passkeeper.utils.DESCrypt
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun encodeTest() {
        val a = DESCrypt.encrypt("加密密文","007158xx")
        println("密文:$a")
        val b = DESCrypt.decrypt(a, "007158xx")
        println("明文:$b")
    }

    @Test
    fun md5test() {
        val a = DESCrypt.MD5("S4$#abcyouSAn")
        println("md5以后的值为: $a")
    }
}