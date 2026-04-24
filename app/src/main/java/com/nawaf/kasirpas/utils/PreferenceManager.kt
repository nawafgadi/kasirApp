package com.nawaf.kasirpas.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.nawaf.kasirpas.model.User

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "kasir_pas_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_IS_ONBOARDED = "is_onboarded"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    fun setLogin(isLogin: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGIN, isLogin).apply()
    }

    fun isLogin(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGIN, false)
    }

    fun setOnboarded(isOnboarded: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ONBOARDED, isOnboarded).apply()
    }

    fun isOnboarded(): Boolean {
        return prefs.getBoolean(KEY_IS_ONBOARDED, false)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
