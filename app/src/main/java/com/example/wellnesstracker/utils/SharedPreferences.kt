package com.example.wellnesstracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 *  Simple, thread-safe SharedPreferences wrapper.
 *
 *  1.  Call  Prefs.init(applicationContext)  once in  Application.onCreate().
 *  2.  Use the put / get helpers anywhere in the app.
 */
object Prefs {

    private const val FILE = "wellness_prefs"
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    /** MUST be called once from your Application class */
    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    /* ------------------------------------------------------------------ *
     *  Primitive helpers                                                 *
     * ------------------------------------------------------------------ */

    fun putInt(key: String, value: Int)            = prefs.edit().putInt(key, value).apply()
    fun getInt(key: String, def: Int = 0)          = prefs.getInt(key, def)

    fun putBool(key: String, value: Boolean)       = prefs.edit().putBoolean(key, value).apply()
    fun getBool(key: String, def: Boolean = false) = prefs.getBoolean(key, def)

    fun putFloat(key: String, value: Float)        = prefs.edit().putFloat(key, value).apply()
    fun getFloat(key: String, def: Float = 0f)     = prefs.getFloat(key, def)

    fun putLong(key: String, value: Long)          = prefs.edit().putLong(key, value).apply()
    fun getLong(key: String, def: Long = 0L)       = prefs.getLong(key, def)

    fun putString(key: String, value: String)      = prefs.edit().putString(key, value).apply()
    fun getString(key: String, def: String = "")   = prefs.getString(key, def) ?: def

    /* ------------------------------------------------------------------ *
     *  Generic object helpers – JSON via Gson                            *
     * ------------------------------------------------------------------ */

    /** Save any serialisable object ( data class, list, map … ) */
    fun <T> putObj(key: String, obj: T) {
        prefs.edit().putString(key, gson.toJson(obj)).apply()
    }

    /**
     * Retrieve an object.
     * Usage:
     *     val type = object : TypeToken<UserSettings>(){}.type
     *     val settings = Prefs.getObj("settings", UserSettings(), type)
     */
    fun <T> getObj(key: String, def: T, type: Type): T {
        val json = prefs.getString(key, null) ?: return def
        return runCatching { gson.fromJson<T>(json, type) }.getOrElse { def }
    }

    /** Remove a key */
    fun remove(key: String) = prefs.edit().remove(key).apply()

    /** Clear *everything* (use carefully!) */
    fun clearAll() = prefs.edit().clear().apply()
}
