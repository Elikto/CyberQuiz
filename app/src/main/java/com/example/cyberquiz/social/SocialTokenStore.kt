package com.example.cyberquiz.social

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object SocialTokenStore {
    private const val KEY_ALIAS = "cyberquiz_social_session_v1"
    private const val PREFERENCES = "cyberquiz_social_secure"
    private const val TOKEN_KEY = "encrypted_token"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    fun save(context: Context, token: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, Base64.encodeToString(payload, Base64.NO_WRAP))
            .apply()
        ProgressSyncManager.request(context)
        AccountEconomyManager.request(context)
        QuestionReportManager.requestFlush(context)
    }

    fun load(context: Context): String? {
        val encoded = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(TOKEN_KEY, null)
            ?: return null
        return runCatching {
            val payload = Base64.decode(encoded, Base64.NO_WRAP)
            require(payload.size > 12)
            val iv = payload.copyOfRange(0, 12)
            val encrypted = payload.copyOfRange(12, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            cipher.doFinal(encrypted).toString(Charsets.UTF_8)
        }.getOrElse {
            clear(context)
            null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .remove(TOKEN_KEY)
            .apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
