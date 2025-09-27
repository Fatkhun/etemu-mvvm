package com.fatkhun.core.utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.reflect.Array
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Locale

fun <T> T.toJson(): String = Gson().toJson(this)

fun String.toJsonObject(): JSONObject = JSONObject(this)

fun sanitizeToString(map: MutableMap<String?, Any?>) {
    for (key in map.keys) {
        val valu = map[key]
        map[key] = if (valu != null && valu is String && !valu.toString()
                .equals("null", ignoreCase = true)
        ) valu else ""
    }
}

@Throws(JSONException::class)
fun arrayToJSON(data: Any): JSONArray? {
    if (!data.javaClass.isArray) {
        throw JSONException("Not a primitive data: " + data.javaClass)
    }
    val length = java.lang.reflect.Array.getLength(data)
    val jsonArray = JSONArray()
    for (i in 0 until length) {
        jsonArray.put(wrap(Array.get(data, i)))
    }
    return jsonArray
}

fun collectionToJSON(data: Collection<*>?): JSONArray? {
    val jsonArray = JSONArray()
    if (data != null) {
        for (aData in data) {
            jsonArray.put(wrap(aData))
        }
    }
    return jsonArray
}

fun mapToJSON(message: Map<*, *>): JSONObject? {
    val obj = JSONObject()
    for ((key1, value) in message) {
        /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
        val key = key1 as String? ?: throw NullPointerException("key == null")
        try {
            obj.put(key, wrap(value))
        } catch (ignored: JSONException) {
        }
    }
    return obj
}

fun mapToQueryString(map: Map<*, *>): String {
    val sb = StringBuilder()
    for ((key, value) in map) {
        if (sb.length > 0) {
            sb.append("&")
        }
        sb.append(
            String.format(
                "%s=%s", key?.let { urlEncodeUTF8(toString(it, "")) },
                value?.let { toString(it, "").let { urlEncodeUTF8(it) } }
            )
        )
    }
    return sb.toString()
}

fun jsonToMap(root: String?): java.util.LinkedHashMap<String?, Any?> {
    return try {
        jsonToMap(JSONObject(root))
    } catch (e: JSONException) {
        LinkedHashMap()
    }
}

@Throws(JSONException::class)
fun jsonToMap(root: JSONObject): LinkedHashMap<String?, Any?> {
    val map = LinkedHashMap<String?, Any?>()
    val keys = root.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val obj = root[key]
        if (obj == null) {
            map[key] = ""
        } else if (obj is JSONObject) {
            map[key] = jsonToMap(obj)
        } else if (obj is JSONArray) {
            map[key] = jsonToList(obj)
        } else {
            map[key] = obj.toString()
        }
    }
    return map
}

fun jsonToList(root: String?): java.util.ArrayList<Any?>? {
    return try {
        jsonToList(JSONArray(root))
    } catch (e: JSONException) {
        ArrayList()
    }
}

@Throws(JSONException::class)
fun jsonToList(root: JSONArray): ArrayList<Any?>? {
    val arr = ArrayList<Any?>()
    for (i in 0 until root.length()) {
        val obj = root[i]
        if (obj == null) {
            arr.add("")
        } else if (obj is JSONObject) {
            arr.add(jsonToMap(obj))
        } else if (obj is JSONArray) {
            arr.add(jsonToList(obj))
        } else {
            arr.add(obj.toString())
        }
    }
    return arr
}

@Throws(UnsupportedEncodingException::class)
fun queryStringToMap(query: String): Map<String, String>? {
    val query_pairs: MutableMap<String, String> = LinkedHashMap()
    val pairs = query.split("&").toTypedArray()
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        query_pairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
            URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
    }
    return query_pairs
}

fun <K, V> getKeyFromValue(hm: Map<K, V>, value: V): K? {
    for (o in hm.keys) {
        if (hm[o] == value) {
            return o
        }
    }
    return null
}

private fun wrap(o: Any?): Any? {
    if (o == null) {
        return null
    }
    if (o is JSONArray || o is JSONObject) {
        return o
    }
    try {
        if (o is Collection<*>) {
            return collectionToJSON(o as Collection<*>?)
        } else if (o.javaClass.isArray) {
            return arrayToJSON(o)
        }
        if (o is Map<*, *>) {
            return mapToJSON(o)
        }
        if (o is Boolean || o is Byte || o is Char
            || o is Double || o is Float || o is Int
            || o is Long || o is Short || o is String
        ) {
            return o
        }
        if (o is CharSequence) {
            return o.toString()
        }
        if (o.javaClass.getPackage().name.startsWith("java.")) {
            return o.toString()
        }
    } catch (ignored: Exception) {
    }
    return null
}

private fun urlEncodeUTF8(s: String): String {
    return try {
        URLEncoder.encode(s, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        throw UnsupportedOperationException(e)
    }
}

fun listToQueryString(key: String, list: List<*>): String? {
    var key = key
    key = urlEncodeUTF8(key)
    val sb = StringBuilder()
    for (i in list.indices) {
        val o = list[i]
        if (o is Map<*, *>) {
            val ee = o
            for (j in ee.keys) {
                if (sb.length > 0) {
                    sb.append("&")
                }
                sb.append(
                    String.format(
                        Locale.getDefault(), "%s[%d][%s]=%s",
                        key, i,
                        urlEncodeUTF8(j.toString()),
                        urlEncodeUTF8(if (ee[j] != null) ee[j].toString() else "")
                    )
                )
            }
        } else if (o is List<*>) {
            val ee = o
            for (j in ee.indices) {
                if (sb.length > 0) {
                    sb.append("&")
                }
                sb.append(
                    String.format(
                        Locale.getDefault(), "%s[%d][%d]=%s",
                        key, i, j,
                        urlEncodeUTF8(if (ee[j] != null) ee[j].toString() else "")
                    )
                )
            }
        } else {
            if (sb.length > 0) {
                sb.append("&")
            }
            sb.append(
                String.format(
                    Locale.getDefault(), "%s[%d]=%s",
                    key, i, urlEncodeUTF8(o?.toString() ?: "")
                )
            )
        }
    }
    return sb.toString()
}

fun arrayListToJSONArray(obj: Any?): String? {
    return try {
        val jsonArray = JSONArray(obj.toString())
        jsonArray.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun toDouble(obj: Any, def: Double): Double {
    if (obj is Double) {
        return obj
    } else if (obj is String) {
        try {
            if (obj.toString().isEmpty()) {
                return 0.0
            }
            return obj.toString().toDouble()
        } catch (ignored: NumberFormatException) {
        }
    }
    return def
}

fun toInt(obj: Any, def: Int): Int {
    if (obj is Int) {
        return obj
    } else if (obj is String) {
        try {
            if (obj.toString().isEmpty()) {
                return 0
            }
            return obj.toString().toInt()
        } catch (ignored: NumberFormatException) {
        }
    }
    return def
}

fun toLong(obj: Any, def: Long): Long {
    if (obj is Long) {
        return obj
    } else if (obj is String) {
        try {
            if (obj.toString().isEmpty()) {
                return 0L
            }
            return obj.toString().toLong()
        } catch (ignored: NumberFormatException) {
        }
    }
    return def
}

fun toFloat(obj: Any, def: Float): Float {
    if (obj is Float) {
        return obj
    } else if (obj is String) {
        try {
            if (obj.toString().isEmpty()) {
                return 0f
            }
            return obj.toString().toFloat()
        } catch (ignored: NumberFormatException) {
        }
    }
    return def
}

fun toBoolean(obj: Any, def: Boolean): Boolean {
    if (obj is Boolean) {
        return obj
    } else if (obj is String) {
        try {
            if (obj.isNull()) {
                return def
            }
            return java.lang.Boolean.parseBoolean(obj.toString())
        } catch (ignored: Exception) {
        }
    }
    return def
}

fun toString(obj: Any, def: String): String {
    if (obj is String || obj is CharSequence) {
        return obj.toString()
    } else {
        try {
            if (obj.isNull()) {
                return def
            }
            return obj.toString()
        } catch (ignored: Exception) {
        }
    }
    return def
}

fun <V> toArrayList(obj: Any?): ArrayList<V>? {
    if (obj is ArrayList<*>) {
        try {
            return obj as ArrayList<V>?
        } catch (ignored: Exception) {
        }
    }
    return ArrayList()
}

fun <K, V> toHashMap(obj: Any?): java.util.HashMap<K, V>? {
    if (obj is java.util.HashMap<*, *>) {
        try {
            return obj as java.util.HashMap<K, V>?
        } catch (ignored: Exception) {
        }
    }
    return HashMap()
}

fun getIgnoreBound(arr: kotlin.Array<String>, index: Int, def: String): String {
    return if (index >= arr.size) {
        def
    } else {
        toString(arr[index], def)
    }
}

fun getIgnoreBound(arr: DoubleArray, index: Int, def: Double): Double {
    return if (index >= arr.size) {
        def
    } else {
        arr[index]
    }
}

fun getIgnoreBound(arr: BooleanArray, index: Int, def: Boolean): Boolean {
    return if (index >= arr.size) {
        def
    } else {
        arr[index]
    }
}

fun urlEncoder(data: String, enc: String = StandardCharsets.UTF_8.toString()): String {
    try {
        return Uri.encode(data)
    } catch (e: UnsupportedEncodingException) {
        return ""
    }
}

inline fun <reified T : Parcelable> Intent.parcelables(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelables(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayLists(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayLists(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int){
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}