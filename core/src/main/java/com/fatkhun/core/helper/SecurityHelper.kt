package com.fatkhun.core.helper

import android.annotation.SuppressLint
import android.os.Build
import android.util.Base64
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecurityHelper {

    fun generateAESKey(myKey: String): SecretKeySpec? {
        val sha: MessageDigest
        try {
            var key: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                myKey.toByteArray(StandardCharsets.UTF_8)
            } else {
                myKey.toByteArray(Charset.forName("UTF-8"))
            }
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = Arrays.copyOf(key, 16)
            return SecretKeySpec(key, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    fun encryptAES(strToEncrypt: String, secret: SecretKeySpec): String? {
        try {
            @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secret)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Base64.encodeToString(
                    cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8)),
                    Base64.DEFAULT
                )
            } else {
                Base64.encodeToString(
                    cipher.doFinal(strToEncrypt.toByteArray(Charset.forName("UTF-8"))),
                    Base64.DEFAULT
                )
            }
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decryptAES(strToDecrypt: String, secret: SecretKeySpec): String? {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secret)
            return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return null

    }
}