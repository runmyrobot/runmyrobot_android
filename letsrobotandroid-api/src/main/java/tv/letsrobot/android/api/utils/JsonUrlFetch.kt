package tv.letsrobot.android.api.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Simple util to get a JSONObject from a url
 * This is called synchronously
 */
object JsonUrlFetch{
    fun getJsonObject(url : String) : JSONObject?{
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
