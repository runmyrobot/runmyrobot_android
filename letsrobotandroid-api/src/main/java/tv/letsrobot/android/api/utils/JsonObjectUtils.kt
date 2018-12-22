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

    /**
     * Get a single string value from the json
     */
    fun getValueJsonObject(url : String, key : String) : String?{
        return JsonObjectUtils.getJsonObjectFromUrl(url)?.let{
            try {
                it.getString(key)
            } catch (e: Exception) {
                null
            }
        }
    }
}
