package tv.letsrobot.android.api.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Simple utility functions for JsonObject
 */
object JsonObjectUtils{

    /**
     * Gets json from url synchronously
     */
    fun getJsonObjectFromUrl(url : String) : JSONObject?{
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(url).build())
        try {
            val response = call.execute()
            if (response.body() != null) {
                return JSONObject(response.body()!!.string())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
