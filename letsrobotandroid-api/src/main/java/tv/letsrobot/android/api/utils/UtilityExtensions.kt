package tv.letsrobot.android.api.utils

import org.json.JSONObject

fun Array<out Any>.getJsonObject() : JSONObject?{
    if(size == 0) return null
    return this[0] as? JSONObject
}