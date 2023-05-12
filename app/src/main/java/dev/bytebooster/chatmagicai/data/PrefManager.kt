package dev.bytebooster.chatmagicai.data

import android.content.Context


class PrefManager constructor(context: Context, prefName: String)  {

    private val sharedPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun setString(key: String, value: String) {
        with (sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun setInt(key: String, value: Int) {
        with (sharedPref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getString(key: String, default: String): String {
        return try {
            sharedPref.getString(key, default)!!
        } catch (e: NullPointerException) {
            default
        }

    }

    fun getInt(key: String, default: Int): Int {
        return sharedPref.getInt(key, default)
    }

}